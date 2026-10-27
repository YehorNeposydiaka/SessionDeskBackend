package org.example.Appointment.DTO;

import jakarta.validation.constraints.NotBlank;

public record AttachBillToAppointmentRequest(
   @NotBlank String billLink
) {}
