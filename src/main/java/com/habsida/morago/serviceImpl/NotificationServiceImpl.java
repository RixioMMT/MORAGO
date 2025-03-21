package com.habsida.morago.serviceImpl;

import com.habsida.morago.exceptions.GraphqlException;
import com.habsida.morago.model.dto.NotificationDTO;
import com.habsida.morago.model.dto.WithdrawalsDTO;
import com.habsida.morago.model.entity.Call;
import com.habsida.morago.model.entity.Notification;
import com.habsida.morago.model.entity.User;
import com.habsida.morago.model.entity.Withdrawals;
import com.habsida.morago.model.inputs.CreateNotificationInput;
import com.habsida.morago.model.inputs.PagingInput;
import com.habsida.morago.model.inputs.UpdateNotificationInput;
import com.habsida.morago.model.results.PageOutput;
import com.habsida.morago.repository.NotificationRepository;
import com.habsida.morago.repository.UserRepository;
import com.habsida.morago.service.NotificationService;
//import com.habsida.morago.service.SmsService;
import com.habsida.morago.util.ModelMapperUtil;
import com.habsida.morago.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final FirebaseService firebaseService;
    private final ModelMapperUtil modelMapperUtil;

    @Transactional(readOnly = true)
    public PageOutput<NotificationDTO> getAllNotification(PagingInput pagingInput) {
//        return notificationRepository.findAll()
//                .stream()
//                .map(notification -> modelMapper.map(notification, NotificationDTO.class))
//                .collect(Collectors.toList());
        Page<Notification> notificationsPage = notificationRepository.findAll(PageUtil.buildPageable(pagingInput));
        return new PageOutput<>(
                notificationsPage.getNumber(),
                notificationsPage.getTotalPages(),
                notificationsPage.getTotalElements(),
                notificationsPage.getContent().stream()
                        .map(withdrawals -> modelMapperUtil.map(notificationsPage, NotificationDTO.class))
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new GraphqlException("Notification not found for id: " + id));
        return modelMapper.map(notification, NotificationDTO.class);
    }

    @Transactional
    public NotificationDTO addNotification(CreateNotificationInput createNotificationInput) {
        if (createNotificationInput.getTitle() == null || createNotificationInput.getTitle().isBlank()
                || createNotificationInput.getText() == null || createNotificationInput.getText().isBlank()) {
            throw new GraphqlException("Notification title and text are required");
        }
        User user = userRepository.findById(createNotificationInput.getUserId())
                .orElseThrow(() -> new GraphqlException("User not found for id: " + createNotificationInput.getUserId()));

        Notification notification = new Notification();
        notification.setTitle(createNotificationInput.getTitle());
        notification.setText(createNotificationInput.getText());
        notification.setTime(LocalTime.now());
        notification.setDate(LocalDate.now());
        notification.setUser(user);

        Notification savedNotification = notificationRepository.save(notification);
        return modelMapper.map(savedNotification, NotificationDTO.class);
    }

    @Transactional
    public NotificationDTO updateNotification(Long id, UpdateNotificationInput updateNotificationInput) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new GraphqlException("Notification not found for id: " + id));

        if (updateNotificationInput.getText() != null && !updateNotificationInput.getText().isBlank()) {
            notification.setText(updateNotificationInput.getText());
        }
        if (updateNotificationInput.getTitle() != null && !updateNotificationInput.getTitle().isBlank()) {
            notification.setTitle(updateNotificationInput.getTitle());
        }
        notification.setTime(LocalTime.now());
        notification.setDate(LocalDate.now());

        Notification updatedNotification = notificationRepository.save(notification);
        return modelMapper.map(updatedNotification, NotificationDTO.class);
    }

    @Transactional
    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new GraphqlException("Notification not found for id: " + id));
        notificationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new GraphqlException("User not found for id: " + userId));
        List<Notification> notifications = notificationRepository.findUserById(userId);
        return notifications.stream()
                .map(notification -> modelMapper.map(notification, NotificationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void notifyCallCreation(User translator, User user) {
        // Create notification for the translator
        CreateNotificationInput translatorNotificationInput = new CreateNotificationInput();
        translatorNotificationInput.setUserId(translator.getId());
        translatorNotificationInput.setTitle("New Call Request");
        String translatorNotificationText = "You have a new call request from " + user.getFirstName();
        translatorNotificationInput.setText(translatorNotificationText);
        addNotification(translatorNotificationInput);
        firebaseService.sendNotification(translator.getFcmToken(), "New Call Request", translatorNotificationText);


        // Create notification for the user
        CreateNotificationInput userNotificationInput = new CreateNotificationInput();
        userNotificationInput.setUserId(user.getId());
        userNotificationInput.setTitle("Call Request Sent");
        String userNotificationText = "Your call request has been sent to " + translator.getFirstName();
        userNotificationInput.setText(userNotificationText);
        addNotification(userNotificationInput);
        firebaseService.sendNotification(user.getFcmToken(), "Call Request Sent", userNotificationText);
    }

    @Override
    @Transactional
    public void notifyCallEnd(User caller, User translator, Call call) {
        // Create notification for the caller
        CreateNotificationInput callerNotificationInput = new CreateNotificationInput();
        callerNotificationInput.setUserId(caller.getId());
        callerNotificationInput.setTitle("Call Ended");
        String userNotificationText = "Your call with " + translator.getFirstName() + " has ended. Duration: " + call.getDuration() + " minutes.";
        callerNotificationInput.setText(userNotificationText);
        addNotification(callerNotificationInput);
        firebaseService.sendSilentNotification(caller.getFcmToken(), "Call Ended", userNotificationText);

        // Create notification for the translator
        CreateNotificationInput translatorNotificationInput = new CreateNotificationInput();
        translatorNotificationInput.setUserId(translator.getId());
        translatorNotificationInput.setTitle("Call Ended");
        String translatorNotificationText = "Your call with " + caller.getFirstName() + " has ended. Duration: " + call.getDuration() + " minutes.";
        translatorNotificationInput.setText(translatorNotificationText);
        addNotification(translatorNotificationInput);
        firebaseService.sendSilentNotification(translator.getFcmToken(), "Call Ended", translatorNotificationText);
    }

    @Override
    @Transactional
    public void notifyConsultantCallCreation(User translator, User user, User consultant) {
        // Create notification for the translator
        CreateNotificationInput translatorNotificationInput = new CreateNotificationInput();
        translatorNotificationInput.setUserId(translator.getId());
        translatorNotificationInput.setTitle("New Call Request");
        String translatorNotificationText = "You have a new consultation call request from " + user.getFirstName();
        translatorNotificationInput.setText(translatorNotificationText);
        addNotification(translatorNotificationInput);
        firebaseService.sendSilentNotification(translator.getFcmToken(), "New Consultation Call Request", translatorNotificationText);

        // Create notification for the consultant
        CreateNotificationInput consultantNotificationInput = new CreateNotificationInput();
        translatorNotificationInput.setUserId(consultant.getId());
        consultantNotificationInput.setTitle("New Call Request");
        String consultantNotificationText = "You have a new consultation call request from " + user.getFirstName();
        consultantNotificationInput.setText(consultantNotificationText);
        addNotification(consultantNotificationInput);
        firebaseService.sendSilentNotification(consultant.getFcmToken(), "New Consultation Call Request", consultantNotificationText);

        // Create notification for the user
        CreateNotificationInput userNotificationInput = new CreateNotificationInput();
        userNotificationInput.setUserId(user.getId());
        userNotificationInput.setTitle("Call Request Sent");
        String userNotificationText = "Your consultation call request has been sent to translator: " + translator.getFirstName() + " and consultant: " + consultant.getFirstName();
        userNotificationInput.setText(userNotificationText);
        addNotification(userNotificationInput);
        firebaseService.sendSilentNotification(user.getFcmToken(), "New Consultation Call Request", userNotificationText);
    }

    @Override
    @Transactional
    public void notifyConsultantCallEnd(User caller, User translator, User consultant, Call call) {
        // Create notification for the caller
        CreateNotificationInput callerNotificationInput = new CreateNotificationInput();
        callerNotificationInput.setUserId(caller.getId());
        callerNotificationInput.setTitle("Call Ended");
        String userNotificationText = "Your consultation call with translator: " + translator.getFirstName() + " and consultant: " + consultant.getFirstName() + " has ended. Duration: " + call.getDuration() + " minutes.";
        callerNotificationInput.setText(userNotificationText);
        addNotification(callerNotificationInput);
        firebaseService.sendSilentNotification(caller.getFcmToken(), "Consultation Call Ended", userNotificationText);

        // Create notification for the translator
        CreateNotificationInput translatorNotificationInput = new CreateNotificationInput();
        translatorNotificationInput.setUserId(translator.getId());
        translatorNotificationInput.setTitle("Call Ended");
        String translatorNotificationText = "Your consultation call with " + caller.getFirstName() + " has ended. Duration: " + call.getDuration() + " minutes.";
        translatorNotificationInput.setText(translatorNotificationText);
        addNotification(translatorNotificationInput);
        firebaseService.sendSilentNotification(translator.getFcmToken(), "Consultation Call Ended", translatorNotificationText);

        // Create notification for the consultant
        CreateNotificationInput consultantNotificationInput = new CreateNotificationInput();
        consultantNotificationInput.setUserId(consultant.getId());
        consultantNotificationInput.setTitle("Call Ended");
        String consultantNotificationText = "Your consultation call with " + caller.getFirstName() + " has ended. Duration: " + call.getDuration() + " minutes.";
        consultantNotificationInput.setText(consultantNotificationText);
        addNotification(consultantNotificationInput);
        firebaseService.sendSilentNotification(consultant.getFcmToken(), "Consultation Call Ended", consultantNotificationText);
    }
}
