package com.bensbible.app.data

import kotlinx.coroutines.flow.Flow

class PresentationRepository(private val dao: PresentationDao) {

    fun getAllPresentations(): Flow<List<PresentationEntity>> = dao.getAllPresentations()

    fun getSlidesForPresentation(presentationId: String): Flow<List<PresentationSlideEntity>> =
        dao.getSlidesForPresentation(presentationId)

    suspend fun createPresentation(name: String): PresentationEntity {
        val entity = PresentationEntity(name = name)
        dao.insertPresentation(entity)
        return entity
    }

    suspend fun deletePresentation(p: PresentationEntity) = dao.deletePresentation(p)

    suspend fun deleteSlide(s: PresentationSlideEntity) = dao.deleteSlide(s)

    suspend fun updateSlide(s: PresentationSlideEntity) = dao.updateSlide(s)

    suspend fun insertSlide(s: PresentationSlideEntity) = dao.insertSlide(s)

    suspend fun getPresentationCount(): Int = dao.getPresentationCount()

    suspend fun addSlides(
        presentationId: String,
        verses: List<Triple<String, Int, Int>>, // bookName, chapter, verse
        texts: List<String>,
        existingCount: Int
    ) {
        verses.forEachIndexed { i, (book, chapter, verse) ->
            val slide = PresentationSlideEntity(
                presentationId = presentationId,
                bookName = book,
                chapterNumber = chapter,
                verseNumber = verse,
                verseText = texts.getOrElse(i) { "" },
                order = existingCount + i
            )
            dao.insertSlide(slide)
        }
    }

    suspend fun seedRomanRoadIfNeeded() {
        if (getPresentationCount() > 0) return

        val presentation = PresentationEntity(name = "Roman Road")
        dao.insertPresentation(presentation)

        val slides = listOf(
            PresentationSlideEntity(
                presentationId = presentation.id,
                bookName = "Romans", chapterNumber = 3, verseNumber = 23,
                verseText = "For all have sinned, and come short of the glory of God;",
                order = 0
            ),
            PresentationSlideEntity(
                presentationId = presentation.id,
                bookName = "Romans", chapterNumber = 6, verseNumber = 23,
                verseText = "For the wages of sin is death; but the gift of God is eternal life through Jesus Christ our Lord.",
                order = 1
            ),
            PresentationSlideEntity(
                presentationId = presentation.id,
                bookName = "Romans", chapterNumber = 5, verseNumber = 8,
                verseText = "But God commendeth his love toward us, in that, while we were yet sinners, Christ died for us.",
                order = 2
            ),
            PresentationSlideEntity(
                presentationId = presentation.id,
                bookName = "Romans", chapterNumber = 10, verseNumber = 9,
                verseText = "That if thou shalt confess with thy mouth the Lord Jesus, and shalt believe in thine heart that God hath raised him from the dead, thou shalt be saved.",
                order = 3
            ),
            PresentationSlideEntity(
                presentationId = presentation.id,
                bookName = "Romans", chapterNumber = 10, verseNumber = 10,
                verseText = "For with the heart man believeth unto righteousness; and with the mouth confession is made unto salvation.",
                order = 4
            ),
            PresentationSlideEntity(
                presentationId = presentation.id,
                bookName = "Romans", chapterNumber = 10, verseNumber = 13,
                verseText = "For whosoever shall call upon the name of the Lord shall be saved.",
                order = 5
            )
        )
        slides.forEach { dao.insertSlide(it) }
    }
}
