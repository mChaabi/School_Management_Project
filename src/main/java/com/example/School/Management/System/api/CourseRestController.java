package com.example.School.Management.System.api;

import com.example.School.Management.System.dto.CourseDto;
import com.example.School.Management.System.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseRestController {

    private final CourseService courseService;

    @GetMapping
    public PagedModel<CourseDto> list(@RequestParam(required = false) String keyword,
                                      @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        Page<CourseDto> page = (keyword != null && !keyword.isBlank())
                ? courseService.searchCourses(keyword, pageable)
                : courseService.getAllCourses(pageable);
        return new PagedModel<>(page);
    }

    @GetMapping("/{id}")
    public CourseDto get(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseDto create(@Valid @RequestBody CourseDto dto) {
        return courseService.createCourse(dto);
    }

    @PutMapping("/{id}")
    public CourseDto update(@PathVariable Long id, @Valid @RequestBody CourseDto dto) {
        return courseService.updateCourse(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}