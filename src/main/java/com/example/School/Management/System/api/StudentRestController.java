package com.example.School.Management.System.api;

import com.example.School.Management.System.dto.StudentDto;
import com.example.School.Management.System.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentRestController {

    private final StudentService studentService;

    @GetMapping
    public PagedModel<StudentDto> list(@RequestParam(required = false) String keyword,
                                       @PageableDefault(size = 10, sort = "lastName") Pageable pageable) {
        Page<StudentDto> page = (keyword != null && !keyword.isBlank())
                ? studentService.searchStudents(keyword, pageable)
                : studentService.getAllStudents(pageable);
        return new PagedModel<>(page);
    }

    @GetMapping("/{id}")
    public StudentDto get(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentDto create(@Valid @RequestBody StudentDto dto) {
        return studentService.createStudent(dto);
    }

    @PutMapping("/{id}")
    public StudentDto update(@PathVariable Long id, @Valid @RequestBody StudentDto dto) {
        return studentService.updateStudent(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }
}