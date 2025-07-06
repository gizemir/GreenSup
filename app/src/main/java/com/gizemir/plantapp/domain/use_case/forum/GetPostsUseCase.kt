package com.gizemir.plantapp.domain.use_case.forum

import com.gizemir.plantapp.domain.model.forum.Post
import com.gizemir.plantapp.domain.repository.forum.ForumRepository
import com.gizemir.plantapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val repository: ForumRepository
) {
    operator fun invoke(): Flow<Resource<List<Post>>> {
        return repository.getAllPosts()
            .map { posts -> Resource.Success(posts) as Resource<List<Post>> }
            .catch { e -> emit(Resource.Error(e.message ?: "An unexpected error occurred")) }
    }
}
