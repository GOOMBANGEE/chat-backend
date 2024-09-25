package com.chat.controller;

import com.chat.dto.EmptyResponseDto;
import com.chat.dto.category.CategoryCreateRequest;
import com.chat.service.category.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  private static final String SERVER_INVALID = "VALID:SERVER_INVALID";
  private static final String CATEGORY_INVALID = "VALID:CATEGORY_INVALID";

  // 카테고리 생성
  @PostMapping("/{serverId}/create")
  public ResponseEntity<EmptyResponseDto> create(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @RequestBody @Valid CategoryCreateRequest requestDto) throws JsonProcessingException {
    categoryService.create(serverId, requestDto);

    return ResponseEntity.ok(null);
  }

  // 카테고리 삭제
  @PostMapping("/{serverId}/{categoryId}/delete")
  public ResponseEntity<EmptyResponseDto> delete(
      @NotNull(message = SERVER_INVALID)
      @PathVariable("serverId")
      Long serverId,
      @NotNull(message = CATEGORY_INVALID)
      @PathVariable("categoryId")
      Long categoryId) {
    categoryService.delete(serverId, categoryId);
    
    return ResponseEntity.ok(null);
  }
}
