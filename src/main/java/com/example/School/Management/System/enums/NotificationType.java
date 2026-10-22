package com.example.School.Management.System.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    //            emoji   bell icon               colour     title             fallback (used if the AI is unavailable)
    ABSENCE(      "🚫", "bi-person-x-fill",        "danger",  "Absence",        "{student} was marked absent on {date} ({classroom})."),
    LATE(         "⏰", "bi-clock-history",        "warning", "Late arrival",   "{student} arrived late on {date} ({classroom})."),
    GRADE_ADDED(  "📝", "bi-journal-check",        "success", "New grade",      "{student} received {grade}/20 in {course}."),
    ENROLLMENT_CREATED("✅", "bi-card-checklist",  "info",    "Enrollment",     "{student} was enrolled in {course}."),
    STUDENT_CREATED("🎓", "bi-mortarboard-fill",   "primary", "New student",    "New student registered: {student} ({classroom})."),
    TEACHER_CREATED("👨‍🏫", "bi-person-workspace", "success", "New teacher",    "New teacher added: {teacher} ({specialization})."),
    COURSE_CREATED("📚", "bi-journal-bookmark-fill","warning","New course",     "New course created: {course}, taught by {teacher}."),
    CLASSROOM_CLOSED("🔒", "bi-door-closed-fill",  "danger",  "Classroom closed","Classroom {classroom} has been closed."),
    SCHEDULE_CHANGED("📅", "bi-calendar-week-fill","info",    "Timetable change","Timetable {action}: {course} in {classroom}, {day} {time}."),
    DOCUMENT_ISSUED("🖨️", "bi-printer-fill",       "info",    "Document issued", "{document} for {student} was issued on {date}."),
    IMPORT_COMPLETED("📥", "bi-upload",            "primary", "Import finished", "Student import finished: {created} of {total} created, {failed} failed."),
    SCHOOL_EVENT( "🗓️", "bi-calendar-event-fill",  "warning", "School calendar", "{kind}: {title} ({dates})."),
    FEE_CREATED(  "💳", "bi-cash-coin",            "warning", "New fee",        "New fee for {student}: {title}, {amount}, due {dueDate}."),
    FEE_PAID(     "🧾", "bi-receipt",              "success", "Payment received","Payment received for {student}: {title}, {amount}. Receipt {receipt}."),
    FEE_OVERDUE(  "⚠️", "bi-exclamation-triangle-fill","danger","Payment overdue","Fee overdue for {student}: {title}, {amount}, was due {dueDate}.");

    private final String emoji;
    private final String bellIcon;
    private final String color;
    private final String title;
    private final String fallback;

    /** prompts/<this>.txt */
    public String promptName() { return name().toLowerCase(); }

    /** "🚫 Sara was marked absent..." for logs, emails, SMS */
    public String format(String message) { return emoji + " " + message; }
}