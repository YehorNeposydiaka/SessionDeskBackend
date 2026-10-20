package org.example.Appointment.Service;

import org.example.Appointment.Repository.AppointmentRepository;
import org.example.WorkingHours.Repository.ScheduleExceptionRepository;
import org.example.WorkingHours.Repository.WorkingHoursRepository;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {
    private final WorkingHoursRepository workingHoursRepository;
    private final ScheduleExceptionRepository scheduleExceptionRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(WorkingHoursRepository workingHoursRepository,
                               ScheduleExceptionRepository scheduleExceptionRepository,
                               AppointmentRepository appointmentRepository){
        this.workingHoursRepository = workingHoursRepository;
        this.scheduleExceptionRepository = scheduleExceptionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public boolean AvailabilityForClient(){
        boolean result = workingHoursCheck() && appointmentCheck() && scheduleExceptionCheck();
        return result;
    }

    public boolean AvailabilityForSpecialist(){
        boolean result = appointmentCheck();
        return result;
    }

    private boolean workingHoursCheck(){
        return true;
    }
    private boolean appointmentCheck(){
        return true;
    }
    private boolean scheduleExceptionCheck(){
        return true;
    }
}
