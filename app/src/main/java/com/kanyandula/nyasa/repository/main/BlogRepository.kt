package com.kanyandula.nyasa.repository.main


import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.repository.JobManager
import com.kanyandula.nyasa.session.SessionManager
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val openApiMainService: NyasaBlogApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("BlogRepository")
{

    private val TAG: String = "AppDebug"


}
















