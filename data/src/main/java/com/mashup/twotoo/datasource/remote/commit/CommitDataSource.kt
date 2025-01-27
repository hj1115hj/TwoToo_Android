package com.mashup.twotoo.datasource.remote.commit

import com.mashup.twotoo.datasource.remote.commit.request.CheerRequest
import com.mashup.twotoo.datasource.remote.commit.request.CommitNoRequest
import com.mashup.twotoo.datasource.remote.commit.request.CommitRequest
import com.mashup.twotoo.datasource.remote.commit.response.Commit
import javax.inject.Inject

class CommitDataSource @Inject constructor(
    private val commitApi: CommitApi,
) {
    suspend fun commit(
        commitRequest: CommitRequest,
    ): Commit {
        return commitApi.commit(
            text = commitRequest.text,
            challengeNo = commitRequest.challengeNo,
            img = commitRequest.img,
        )
    }

    suspend fun getCommitByNo(
        commitNoRequest: CommitNoRequest,
    ): Commit {
        return commitApi.getCommitByNo(commitNo = commitNoRequest.commitNo)
    }

    suspend fun cheerByNo(
        commitNoRequest: CommitNoRequest,
        cheerRequest: CheerRequest,
    ): Commit {
        return commitApi.cheerByNo(
            commitNo = commitNoRequest.commitNo,
            cheerRequest = cheerRequest,
        )
    }
}
