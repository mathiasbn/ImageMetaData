package dk.hoeghbirkkjaer.exifchanger

import spock.lang.Specification

class ExifChangerTest extends Specification{


    public static final String SOME_JPG = "IMG_2206.JPG"
    public static final SOME_JPG_FILE = new File("src/test/resources/$SOME_JPG")
    private ImageMetaData imageMetaData

    void setup() {
        imageMetaData = imageMetaDataFromFilePath(SOME_JPG)
    }

    private ImageMetaData imageMetaDataFromFilePath(String some_jpg) {
        new ImageMetaData(getClass().getClassLoader().getResourceAsStream(some_jpg))
    }

    def "When an JPG image is loaded, then the width can be retrieved"() {
        expect:
        imageMetaData.width == 3648
    }

    def "When an JPG image is loaded, then the creation date can be retrieved"() {
        expect:
        imageMetaData.creationDate.before new Date()
    }

    def "Given an image metadate, when storing a new creation date, then the image metadata is updated"() {
        given:
        def calendar = new Date().toCalendar()
        calendar.set(Calendar.MILLISECOND,0)
        def newCreationDate = calendar.time

        when:
        imageMetaData.creationDate = newCreationDate
        imageMetaData.store(SOME_JPG_FILE.newOutputStream())

        then:
        new ImageMetaData(SOME_JPG_FILE.newInputStream()).creationDate == newCreationDate
    }

    def "Given an image metadata, then it is possible to store a string as title"() {
        when:
        def newTitle = "new title at ${new Date()}"
        imageMetaData.title = newTitle
        imageMetaData.store(SOME_JPG_FILE.newOutputStream())

        then:
        new ImageMetaData(SOME_JPG_FILE.newInputStream()).title == newTitle

    }
}
