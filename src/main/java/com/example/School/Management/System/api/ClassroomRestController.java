package com.example.School.Management.System.api;

import com.example.School.Management.System.dto.ClassroomDto;
import com.example.School.Management.System.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomRestController {

    private final ClassroomService classroomService;

    @GetMapping
    public PagedModel<ClassroomDto> list(@PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return new PagedModel<>(classroomService.getAllClassrooms(pageable));
    }

    @GetMapping("/{id}")
    public ClassroomDto get(@PathVariable Long id) {
        return classroomService.getClassroomById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassroomDto create(@Valid @RequestBody ClassroomDto dto) {
        return classroomService.createClassroom(dto);
    }

    @PutMapping("/{id}")
    public ClassroomDto update(@PathVariable Long id, @Valid @RequestBody ClassroomDto dto) {
        return classroomService.updateClassroom(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
    }
}