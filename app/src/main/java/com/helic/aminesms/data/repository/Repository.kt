package com.helic.aminesms.data.repository

import com.helic.aminesms.data.RemoteDataSource
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class Repository @Inject constructor(remoteDataSource: RemoteDataSource) {

    val remote = remoteDataSource
}