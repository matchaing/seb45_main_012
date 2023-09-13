package com.green.greenEarthForUs.post.Controller;

import com.green.greenEarthForUs.Exception.ImageDeletionException;
import com.green.greenEarthForUs.Image.Service.ImageService;
import com.green.greenEarthForUs.post.DTO.PostPatchDto;
import com.green.greenEarthForUs.post.DTO.PostPostDto;
import com.green.greenEarthForUs.post.DTO.PostResponseDto;
import com.green.greenEarthForUs.post.Entity.Post;
import com.green.greenEarthForUs.post.Mapper.PostMapper;
import com.green.greenEarthForUs.post.Service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/post")
public class PostController {

    final PostService postService;
    private final PostMapper mapper;

    private final ImageService imageService;
    @Autowired
    public PostController(PostService postService, PostMapper mapper, ImageService imageService) {
        this.postService = postService;
        this.mapper = mapper;
        this.imageService = imageService;
    }

    // 게시글 생성
    @PostMapping("/{user-id}")
    public ResponseEntity<PostResponseDto> createPost(@PathVariable(value = "user-id") Long userId,
                                                      @RequestPart(value = "image", required = false) List<MultipartFile> image,
                                                      @RequestPart(value = "json") PostPostDto postPostDto) throws IOException {

        Post createdPost = postService.createPost(userId, postPostDto);

        if(image != null){
            List<String> images = new ArrayList<>();
            for(MultipartFile file : image) {
                String imageUrl = imageService.uploadImage(file);
                images.add(imageUrl);
            }
            createdPost.setImageUrls(images);
        }

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
    @GetMapping("/all")
    public ResponseEntity<List<PostResponseDto>> getPostAll() {
        List<PostResponseDto> postList = postService.getAllPosts();
        return new ResponseEntity<>(postList, HttpStatus.OK);
    }

    // 게시판 별 게시글 조회
    @GetMapping("/type/{type}")
    public ResponseEntity<List<PostResponseDto>> getPostByType(@PathVariable("type") String type) {
        List<PostResponseDto> postList = postService.getPostsByType(type);
        return new ResponseEntity<>(postList, HttpStatus.OK);
    }

    // 사용자 별 조회
    @GetMapping("/customer/{user-id}")
    public ResponseEntity<List<PostResponseDto>> getPostsByUserId(@PathVariable(value = "user-id") Long userId) {
        List<PostResponseDto> posts = postService.getPostsByUserId(userId);
        return ResponseEntity.ok(posts);
    }

    // 게시글 수정
    @PatchMapping("/{user-id}/{post-id}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable(value = "user-id") Long userId,
                                                      @PathVariable(value = "post-id") Long postId,
                                                      @RequestPart(value = "image", required = false) List<MultipartFile> image,
                                                      @RequestPart(value = "json") PostPatchDto postPatchDto) throws Exception{


        PostResponseDto updatedPost = postService.updatePost(userId, postId, postPatchDto);
        if(image != null){
            List<String> images = new ArrayList<>();
            for(MultipartFile file : image) {
                String imageUrl = imageService.uploadImage(file);
                images.add(imageUrl);
            }
            updatedPost.setImageUrls(images);
        }

        return ResponseEntity.ok(updatedPost);
    }

    // 게시글 삭제
    @DeleteMapping("/{user-id}/{post-id}")
    public ResponseEntity<Void> deletePost(@PathVariable(value = "user-id") Long userId,
                                           @PathVariable(value = "post-id") Long postId) throws ImageDeletionException {
        postService.deletePost(postId, userId);

        return ResponseEntity.noContent().build();
    }
}