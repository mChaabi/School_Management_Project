package com.example.School.Management.System.api;

import com.example.School.Management.System.dto.EnrollmentDto;
import com.example.School.Management.System.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentRestController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public List<EnrollmentDto> list() {
        return enrollmentService.findAll();
    }

    @GetMapping("/{id}")
    public EnrollmentDto get(@PathVariable Long id) {
        return enrollmentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentDto create(@Valid @RequestBody EnrollmentDto dto) {
        return enrollmentService.create(dto);
    }

    @PutMapping("/{id}")
    public EnrollmentDto update(@PathVariable Long id, @Valid @RequestBody EnrollmentDto dto) {
        return enrollmentService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        enrollmentService.delete(id);
    }
}