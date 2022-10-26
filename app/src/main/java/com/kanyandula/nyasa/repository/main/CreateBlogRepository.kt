package com.kanyandula.nyasa.repository.main


import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.repository.JobManager
import com.kanyandula.nyasa.session.SessionManager
import javax.inject.Inject

class CreateBlogRepository
@Inject
constructor(
    val openApiMainService: NyasaBlogApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("CreateBlogRepository") {

    private val TAG: String = "AppDebug"


}
















