package dk.hoeghbirkkjaer.exifchanger
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoXpString
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet

import java.text.SimpleDateFormat

class ImageMetaData {
    public static final String DATE_FORMAT = "yyyy:MM:dd HH:mm:ss"
    public static final TagInfoAscii DATE_TIME_ORIGINAL_TAG = ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL
    public static final TagInfoXpString TITLE_TAG = MicrosoftTagConstants.EXIF_TAG_XPTITLE
    JpegImageMetadata metadata
    private String newCreationDate
    private byte[] image
    private String newTitle
    private List updates = []

    ImageMetaData(InputStream image) {
        this.image = image.bytes
        this.metadata = Imaging.getMetadata(this.image) as JpegImageMetadata
    }

    public int getWidth() {
        metadata.findEXIFValue(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH).doubleValue.toInteger()
    }

    String getTitle() {
        metadata.findEXIFValue(TITLE_TAG).stringValue
    }

    void setTitle(String title) {
        newTitle = title
        updates.add {TiffOutputDirectory directory ->
            directory.removeField(TITLE_TAG)
            directory.add(TITLE_TAG, newTitle )
        }
    }

    public Date getCreationDate() {
        def createDateText = metadata.findEXIFValue(DATE_TIME_ORIGINAL_TAG).stringValue
        Date.parse(DATE_FORMAT, createDateText)
    }

    void setCreationDate(Date creationDate) {
        newCreationDate = new SimpleDateFormat(DATE_FORMAT).format(creationDate)
        updates.add {
            TiffOutputDirectory directory ->
                directory.removeField(DATE_TIME_ORIGINAL_TAG)
                directory.add(DATE_TIME_ORIGINAL_TAG, newCreationDate )
        }
    }

    def store(OutputStream to) {
        def outputSet = metadata.getExif().getOutputSet()
        if (null == outputSet) {
            outputSet = new TiffOutputSet();
        }
        def directory = outputSet.getOrCreateExifDirectory()

        updates.each {Closure updateCommand -> updateCommand(directory)}

        new ExifRewriter().updateExifMetadataLossless(image, to, outputSet);
    }

}
