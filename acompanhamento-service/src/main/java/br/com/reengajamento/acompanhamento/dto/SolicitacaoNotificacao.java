package br.com.reengajamento.acompanhamento.dto;

public class SolicitacaoNotificacao {

    private String requestId;
    private Long courseId;
    private Long assignmentId;
    private Long userId;
    private String studentName;
    private String studentEmail;
    private String assignmentName;
    private Long dueDate;
    private String link;

    public SolicitacaoNotificacao() {
    }

    public SolicitacaoNotificacao(
            String requestId,
            Long courseId,
            Long assignmentId,
            Long userId,
            String studentName,
            String studentEmail,
            String assignmentName,
            Long dueDate,
            String link) {
        this.requestId = requestId;
        this.courseId = courseId;
        this.assignmentId = assignmentId;
        this.userId = userId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.assignmentName = assignmentName;
        this.dueDate = dueDate;
        this.link = link;
    }

    public String getRequestId() {
        return requestId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public String getLink() {
        return link;
    }
}