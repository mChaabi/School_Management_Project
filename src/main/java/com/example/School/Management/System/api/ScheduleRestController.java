package com.example.School.Management.System.api;

import com.example.School.Management.System.dto.ScheduleDto;
import com.example.School.Management.System.repository.ScheduleRepository;
import com.example.School.Management.System.service.NotificationService;
import com.example.School.Management.System.service.ScheduleService;
import com.example.School.Management.System.repository.ClassroomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleRestController {

    private final ScheduleService service;
    private final ClassroomRepository classroomRepo;
    private final NotificationService notificationService;
    private final ScheduleRepository scheduleRepository;

    // 1. Obtener todos los horarios (o filtrar por aula / profesor)
    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getAllSchedules(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long teacherId) {

        // Obtenemos todas las entidades y las convertimos a DTOs usando el servicio o el mapper que ya tengas
        List<ScheduleDto> schedules = scheduleRepository.findAll().stream()
                .map(schedule -> service.findDto(schedule.getId()))
                .toList();

        return ResponseEntity.ok(schedules);
    }

    // 2. Obtener un horario por ID
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDto> getScheduleById(@PathVariable Long id) {
        ScheduleDto dto = service.findDto(id);
        return ResponseEntity.ok(dto);
    }

    // 3. Crear un nuevo horario (POST)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSchedule(@Valid @RequestBody ScheduleDto dto) {
        service.save(null, dto.getCourseId(), dto.getClassroomId(),
                dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime());

        // Notificación opcional de IA
        var classroom = classroomRepo.findById(dto.getClassroomId()).orElse(null);
        String classroomName = (classroom != null) ? classroom.getName() : "Unknown";

        notificationService.onScheduleChanged(
                dto.getCourseId(),
                dto.getClassroomId(),
                classroomName,
                "added",
                dto.getDayOfWeek().name(),
                dto.getStartTime().toString()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Lesson added successfully", "status", true));
    }

    // 4. Actualizar un horario existente (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleDto dto) {

        service.save(id, dto.getCourseId(), dto.getClassroomId(),
                dto.getDayOfWeek(), dto.getStartTime(), dto.getEndTime());

        var classroom = classroomRepo.findById(dto.getClassroomId()).orElse(null);
        String classroomName = (classroom != null) ? classroom.getName() : "Unknown";

        notificationService.onScheduleChanged(
                dto.getCourseId(),
                dto.getClassroomId(),
                classroomName,
                "updated",
                dto.getDayOfWeek().name(),
                dto.getStartTime().toString()
        );

        return ResponseEntity.ok(Map.of("message", "Lesson updated successfully", "status", true));
    }

    // 5. Eliminar un horario (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Lesson deleted successfully", "status", true));
    }
}
