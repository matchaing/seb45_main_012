package com.green.greenEarthForUs.post.Service;

import com.green.greenEarthForUs.Exception.UnauthorizedException;
import com.green.greenEarthForUs.post.DTO.PostPatchDto;
import com.green.greenEarthForUs.post.DTO.PostPostDto;
import com.green.greenEarthForUs.post.DTO.PostResponseDto;
import com.green.greenEarthForUs.post.Entity.Post;
import com.green.greenEarthForUs.post.Mapper.PostMapper;
import com.green.greenEarthForUs.post.Repository.PostRepository;
import com.green.greenEarthForUs.user.Entity.User;
import com.green.greenEarthForUs.user.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class PostService {

    private final PostRepository postsRepository;
    private final UserRepository userRepository;
    private final PostMapper mapper;

    @Autowired
    public PostService(PostRepository postsRepository, UserRepository userRepository, PostMapper mapper) {
        this.postsRepository = postsRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    // 게시글 생성
    @Transactional
    public Post createPost(Long userId, PostPostDto postPostDto) { // 유저, 게시글

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Post post = mapper.PostDtoToPost(postPostDto);
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());

        return postsRepository.save(post);
    }

    // 단일 게시글 조회
    @Transactional
    public PostResponseDto getPost(Long postId) {
        Post post = postsRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post not found" + postId));

        return mapper.postToPostResponseDto(post);
    }

    // 모든 게시글 조회
    @Transactional
    public List<PostResponseDto> getAllPosts() {
        List<Post> posts = postsRepository.findAll();
        return posts.stream()
                .map(mapper::postToPostResponseDto)
                .collect(Collectors.toList());
    }

    // 게시판 별 게시글 조회
    @Transactional
    public List<PostResponseDto> getPostsByType(String typeName) {
        List<Post> posts = postsRepository.findByType(typeName);
        return posts.stream()
                .map(mapper::postToPostResponseDto)
                .collect(Collectors.toList());
    }

    // 사용자 별 게시글 조회
    @Transactional
    public List<PostResponseDto> getPostsByUserId(Long userId) {
        User user = new User();
        user.setUserId(userId);
        List<Post> posts = postsRepository.findByUser(user);
        return posts.stream()
                .map(mapper::postToPostResponseDto)
                .collect(Collectors.toList());
    }
    // 게시글 수정
    @Transactional
    public PostResponseDto updatePost(Long userId, Long postId, PostPatchDto postPatchDto) {

        Post existingPost = postsRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // 게시글의 작성자와 요청한 사용자가 일치하는지 확인
        User user = existingPost.getUser();
        if (!user.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to update this post.");
        }

        // 새로운 내용으로 게시글 업데이트
        existingPost.setType(postPatchDto.getType());
        existingPost.setTitle(postPatchDto.getTitle());
        existingPost.setBody(postPatchDto.getBody());
        existingPost.setOpen(postPatchDto.getOpen());

        // 업데이트된 게시글 저장
        Post updatedPost = postsRepository.save(existingPost);

        return mapper.postToPostResponseDto(updatedPost);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post existingPost = postsRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        // 작성자 요청자 일치하는지
        User user = existingPost.getUser();
        if (!user.getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this post");
        }

        postsRepository.delete(existingPost);
    }


//    페이지네이션
//    @Transactional
//    public Page<Post> getLatestPosts(int page) {
//
//        int pageNumber = page > 0 ? page - 1 : 0;
//
//        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createdAt").descending());
//
//        return postsRepository.findAll(pageable);
//    }
}

