package com.habsida.morago.resolver;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.habsida.morago.model.dto.CallDTO;
import com.habsida.morago.model.enums.CallStatus;
import com.habsida.morago.model.inputs.CallUpdateInput;
import com.habsida.morago.model.inputs.CreateCallInput;
import com.habsida.morago.model.inputs.CreateConsultantCallInput;
import com.habsida.morago.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CallMutationResolver implements GraphQLMutationResolver {
    private final CallService callService;

    public CallDTO createCall(CreateCallInput input) throws Exception {
        return callService.createCall(input.getChannelName(), input.getCaller(), input.getRecipient(), input.getTheme());
    }

    public CallDTO updateCall(Long id, CallUpdateInput input) {
        Float commission = (input.getCommission() != null) ? input.getCommission().floatValue() : null;
        return callService.updateCall(id, input.getStatus(), input.getDuration(), commission);
    }

    public Boolean deleteCall(Long id) throws Exception {
        callService.deleteCall(id);
        return true;
    }

    public Boolean endCall(Long callId, CallStatus status, Integer duration) {
        callService.endCall(callId, status, duration);
        return true;
    }

    public Boolean rateCall(Long userId, Long callId, double grade) {
        callService.rateCall(userId, callId, grade);
        return true;
    }

    public CallDTO createConsultantCall(CreateConsultantCallInput input) throws Exception {
        return callService.createConsultantCall(input.getChannelName(), input.getCaller(), input.getRecipient(), input.getRecipientConsultant(), input.getTheme());
    }

    public Boolean endConsultantCall(Long callId, CallStatus status, Integer duration) {
        callService.endConsultantCall(callId, status, duration);
        return true;
    }

    public Boolean rateConsultantCall(Long userId, Long callId, double translatorGrade, double consultantGrade) {
        callService.rateConsultantCall(userId, callId, translatorGrade, consultantGrade);
        return true;
    }
}
