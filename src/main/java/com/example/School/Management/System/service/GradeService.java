package com.example.School.Management.System.service;

import com.example.School.Management.System.dto.GradeRow;
import com.example.School.Management.System.dto.GradeSheetDto;
import com.example.School.Management.System.entity.Course;
import com.example.School.Management.System.entity.Enrollment;
import com.example.School.Management.System.repository.CourseRepository;
import com.example.School.Management.System.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final EnrollmentRepository enrollmentRepo;
    private final CourseRepository courseRepo;

    @Transactional(readOnly = true)
    public GradeSheetDto loadSheet(Long courseId) {
        Course course = courseRepo.findById(courseId).orElseThrow();
        GradeSheetDto sheet = new GradeSheetDto();
        sheet.setCourseId(courseId);
        sheet.setCourseTitle(course.getTitle());

        for (Enrollment e : enrollmentRepo.findByCourseId(courseId)) {
            GradeRow row = new GradeRow();
            row.setEnrollmentId(e.getId());
            row.setStudentName(e.getStudent().getFirstName() + " " + e.getStudent().getLastName());
            row.setGrade(e.getGrade());
            sheet.getRows().add(row);
        }
        return sheet;
    }

    @Transactional
    public void save(Long courseId, GradeSheetDto sheet) {
        for (GradeRow row : sheet.getRows()) {
            Enrollment e = enrollmentRepo.findById(row.getEnrollmentId()).orElseThrow();
            // security: make sure this enrollment really belongs to this course
            if (!e.getCourse().getId().equals(courseId)) {
                throw new AccessDeniedException("Wrong course");
            }
            e.setGrade(row.getGrade());
        }
    }
}
