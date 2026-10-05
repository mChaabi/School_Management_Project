package com.example.School.Management.System.api;

import com.example.School.Management.System.dto.TeacherDto;
import com.example.School.Management.System.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherRestController {

    private final TeacherService teacherService;

    @GetMapping
    public PagedModel<TeacherDto> list(@RequestParam(required = false) String keyword,
                                       @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        Page<TeacherDto> page = (keyword != null && !keyword.isBlank())
                ? teacherService.searchTeachers(keyword, pageable)
                : teacherService.getAllTeachers(pageable);
        return new PagedModel<>(page);
    }

    @GetMapping("/{id}")
    public TeacherDto get(@PathVariable Long id) {
        return teacherService.getTeacherById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherDto create(@Valid @RequestBody TeacherDto dto) {
        return teacherService.createTeacher(dto);
    }

    @PutMapping("/{id}")
    public TeacherDto update(@PathVariable Long id, @Valid @RequestBody TeacherDto dto) {
        return teacherService.updateTeacher(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
    }
}