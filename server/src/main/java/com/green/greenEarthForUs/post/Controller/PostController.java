package com.green.greenEarthForUs.post.Controller;

import com.green.greenEarthForUs.post.DTO.PostPatchDto;
import com.green.greenEarthForUs.post.DTO.PostPostDto;
import com.green.greenEarthForUs.post.DTO.PostResponseDto;
import com.green.greenEarthForUs.post.Entity.Post;
import com.green.greenEarthForUs.post.Mapper.PostMapper;
import com.green.greenEarthForUs.post.Service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/post")
public class PostController {

    final PostService postService;
    private final PostMapper mapper;

    public PostController(PostService postService, PostMapper mapper) {
        this.postService = postService;
        this.mapper = mapper;
    }

    // 게시글 생성
    @PostMapping("/{user-id}")
    public ResponseEntity<PostResponseDto> createPost(@PathVariable(value = "user-id") Long userId,
                                                      @ModelAttribute PostPostDto postPostDto,
                                                      MultipartHttpServletRequest request) throws IOException {

        // 파일
        MultipartFile multipartFile = request.getFile("image");
        if (multipartFile != null && !multipartFile.isEmpty()) {
            // 파일 이름 가져오기
            String originalFileName = multipartFile.getOriginalFilename();
            // 파일 내용(byte 배열) 가져오기
            byte[] fileContent = multipartFile.getBytes();

            postPostDto.setBodyImageFileName(originalFileName);
            postPostDto.setBodyImage(fileContent);
        }

        Post createdPost = postService.createPost(userId, postPostDto);
        PostResponseDto responseDto = mapper.postToPostResponseDto(createdPost);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 단일 게시글 조회
    @GetMapping("/{post-id}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable(value="post-id") Long postId) {
        PostResponseDto responseDto = postService.getPost(postId);

        if (responseDto != null) {
            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 모든 게시글 조회
    @GetMapping("/")
    public ResponseEntity<List<PostResponseDto>> getPostAll() {
        List<PostResponseDto> postList = postService.getAllPosts();
        return new ResponseEntity<>(postList, HttpStatus.OK);
    }

    // 게시판 별 게시글 조회
    @GetMapping("/type/{type}")
    public ResponseEntity<List<PostResponseDto>> getPostByType(@PathVariable String type) {
        List<PostResponseDto> postList = postService.getPostsByType(type);
        return new ResponseEntity<>(postList, HttpStatus.OK);
    }

    // 사용자 별 조회
    @GetMapping("/user/{user-id}")
    public ResponseEntity<List<PostResponseDto>> getPostsByUserId(@PathVariable(value = "user-id") Long userId) {
        List<PostResponseDto> posts = postService.getPostsByUserId(userId);
        return ResponseEntity.ok(posts);
    }

    // 게시글 수정
    @PatchMapping("/{user-id}/{post-id}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable(value = "user-id") Long userId,
                                                      @PathVariable(value = "post-id") Long postId,
                                                      @RequestBody PostPatchDto postPatchDto) {

        PostResponseDto updatedPost = postService.updatePost(userId, postId, postPatchDto);
        return ResponseEntity.ok(updatedPost);
    }

    // 게시글 삭제
    @DeleteMapping("/{user-id}/{post-id}")
    public ResponseEntity<Void> deletePost(@PathVariable(value = "user-id") Long userId,
                                           @PathVariable(value = "post-id") Long postId) {
        postService.deletePost(postId, userId);

        return ResponseEntity.noContent().build();
    }
}