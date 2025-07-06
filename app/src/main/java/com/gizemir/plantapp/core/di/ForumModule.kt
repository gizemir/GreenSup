package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.data.repository.forum.ForumRepositoryImpl
import com.gizemir.plantapp.data.source.forum.FirebaseForumDataSource
import com.gizemir.plantapp.data.source.forum.ForumRemoteDataSource
import com.gizemir.plantapp.data.local.dao.forum.PostDao
import com.gizemir.plantapp.data.local.dao.forum.CommentDao
import com.gizemir.plantapp.domain.repository.forum.ForumRepository
import com.gizemir.plantapp.domain.use_case.forum.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ForumModule {

    @Provides
    @Singleton
    fun provideForumRemoteDataSource(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage
    ): ForumRemoteDataSource {
        return FirebaseForumDataSource(firestore, auth, storage)
    }

    @Provides
    @Singleton
    fun provideForumRepository(
        remoteDataSource: ForumRemoteDataSource,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        postDao: PostDao,
        commentDao: CommentDao
    ): ForumRepository {
        return ForumRepositoryImpl(remoteDataSource, auth, firestore, postDao, commentDao)
    }

    @Provides
    @Singleton
    fun provideGetPostsUseCase(repository: ForumRepository): GetPostsUseCase {
        return GetPostsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetPostByIdUseCase(repository: ForumRepository): GetPostByIdUseCase {
        return GetPostByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreatePostUseCase(repository: ForumRepository): CreatePostUseCase {
        return CreatePostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLikePostUseCase(repository: ForumRepository): LikePostUseCase {
        return LikePostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddCommentUseCase(repository: ForumRepository): AddCommentUseCase {
        return AddCommentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteCommentUseCase(repository: ForumRepository): DeleteCommentUseCase {
        return DeleteCommentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideForumUseCases(
        getPostsUseCase: GetPostsUseCase,
        getPostByIdUseCase: GetPostByIdUseCase,
        createPostUseCase: CreatePostUseCase,
        likePostUseCase: LikePostUseCase,
        addCommentUseCase: AddCommentUseCase,
        deleteCommentUseCase: DeleteCommentUseCase
    ): ForumUseCases {
        return ForumUseCases(
            getPosts = getPostsUseCase,
            getPostById = getPostByIdUseCase,
            createPost = createPostUseCase,
            likePost = likePostUseCase,
            addComment = addCommentUseCase,
            deleteComment = deleteCommentUseCase
        )
    }
}
