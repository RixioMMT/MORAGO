package com.habsida.morago;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.habsida.morago.model.enums.ESort;
import com.habsida.morago.exceptions.ExceptionGraphql;
import com.habsida.morago.model.dto.UserDTO;
import com.habsida.morago.model.dto.UserProfileDTO;
import com.habsida.morago.model.entity.User;
import com.habsida.morago.model.entity.UserProfile;
import com.habsida.morago.model.inputs.PagingInput;
import com.habsida.morago.model.results.UserProfilePageOutput;
import com.habsida.morago.repository.UserProfileRepository;
import com.habsida.morago.repository.UserRepository;
import com.habsida.morago.serviceImpl.UserProfileService;
import com.habsida.morago.util.ModelMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ModelMapperUtil modelMapperUtil;

    @InjectMocks
    private UserProfileService userProfileService;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void testGetAllUserProfiles() {
        PagingInput pagingInput = new PagingInput(0, 10, "id", ESort.DES);
        Pageable pageable = PageRequest.of(pagingInput.getPageNo(), pagingInput.getPageSize(), Sort.by(Sort.Direction.DESC, pagingInput.getSortBy()));

        UserProfile userProfileFirst = new UserProfile();
        userProfileFirst.setId(1L);
        userProfileFirst.setIsFreeCallMade(true);

        UserProfile userProfileSecond = new UserProfile();
        userProfileSecond.setId(2L);
        userProfileSecond.setIsFreeCallMade(false);

        List<UserProfile> userProfiles = Arrays.asList(userProfileFirst, userProfileSecond);
        Page<UserProfile> userProfilePage = new PageImpl<>(userProfiles, pageable, userProfiles.size());

        UserProfileDTO userProfileDTOFirst = new UserProfileDTO();
        userProfileDTOFirst.setId(1L);
        userProfileDTOFirst.setIsFreeCallMade(true);

        UserProfileDTO userProfileDTOSecond = new UserProfileDTO();
        userProfileDTOSecond.setId(2L);
        userProfileDTOSecond.setIsFreeCallMade(false);

        when(userProfileRepository.findAll(any(Pageable.class))).thenReturn(userProfilePage);
        when(modelMapperUtil.map(userProfileFirst, UserProfileDTO.class)).thenReturn(userProfileDTOFirst);
        when(modelMapperUtil.map(userProfileSecond, UserProfileDTO.class)).thenReturn(userProfileDTOSecond);

        UserProfilePageOutput result = userProfileService.getAllUserProfiles(pagingInput);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getObjectList().size());
        assertEquals(userProfileDTOFirst.getId(), result.getObjectList().get(0).getId());
        assertEquals(userProfileDTOSecond.getId(), result.getObjectList().get(1).getId());
        verify(userProfileRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    public void testGetUserProfileById() throws ExceptionGraphql {
        Long id = 1L;
        UserProfile userProfile = new UserProfile();
        userProfile.setId(id);
        userProfile.setIsFreeCallMade(true);

        UserProfileDTO expectedUserProfileDTO = new UserProfileDTO();
        expectedUserProfileDTO.setId(id);
        expectedUserProfileDTO.setIsFreeCallMade(true);

        when(userProfileRepository.findById(id)).thenReturn(java.util.Optional.of(userProfile));
        when(modelMapperUtil.map(userProfile, UserProfileDTO.class)).thenReturn(expectedUserProfileDTO);

        UserProfileDTO result = userProfileService.getUserProfileById(id);

        assertNotNull(result);
        assertEquals(expectedUserProfileDTO.getId(), result.getId());
        assertEquals(expectedUserProfileDTO.getIsFreeCallMade(), result.getIsFreeCallMade());
        verify(userProfileRepository, times(1)).findById(id);
    }

    @Test
    public void testAddUserProfile() throws ExceptionGraphql {
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        User user = User.builder()
                .id(1L)
                .userProfile(null)
                .translatorProfile(null)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapperUtil.map(any(UserProfile.class), eq(UserProfileDTO.class))).thenReturn(userProfileDTO);

        UserProfileDTO result = userProfileService.addUserProfile(true, 1L);

        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUserProfile() throws ExceptionGraphql {
        Long id = 1L;
        Boolean updatedIsFreeCallMade = true;
        UserProfile userProfile = new UserProfile();
        userProfile.setId(id);
        userProfile.setIsFreeCallMade(false);

        UserProfile updatedUserProfile = new UserProfile();
        updatedUserProfile.setId(id);
        updatedUserProfile.setIsFreeCallMade(updatedIsFreeCallMade);

        UserProfileDTO expectedUserProfileDTO = new UserProfileDTO();
        expectedUserProfileDTO.setId(id);
        expectedUserProfileDTO.setIsFreeCallMade(updatedIsFreeCallMade);

        when(userProfileRepository.findById(id)).thenReturn(java.util.Optional.of(userProfile));
        when(userProfileRepository.save(userProfile)).thenReturn(updatedUserProfile);
        when(modelMapperUtil.map(updatedUserProfile, UserProfileDTO.class)).thenReturn(expectedUserProfileDTO);

        UserProfileDTO result = userProfileService.updateUserProfile(id, updatedIsFreeCallMade);

        assertNotNull(result);
        assertEquals(expectedUserProfileDTO.getId(), result.getId());
        assertEquals(expectedUserProfileDTO.getIsFreeCallMade(), result.getIsFreeCallMade());
        verify(userProfileRepository, times(1)).findById(id);
        verify(userProfileRepository, times(1)).save(userProfile);
    }

    @Test
    public void testDeleteUserProfile() throws ExceptionGraphql {
        Long id = 1L;
        User user = new User();
        user.setId(1L);

        UserProfile userProfile = new UserProfile();
        userProfile.setId(id);
        userProfile.setUser(user);

        when(userProfileRepository.findById(id)).thenReturn(java.util.Optional.of(userProfile));
        doNothing().when(userProfileRepository).deleteById(id);
        when(userRepository.save(user)).thenReturn(user);

        userProfileService.deleteUserProfile(id);

        assertNull(user.getUserProfile());
        verify(userProfileRepository, times(1)).findById(id);
        verify(userProfileRepository, times(1)).deleteById(id);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testUpdateUserProfileByUserId() throws ExceptionGraphql {
        Long userId = 1L;
        Boolean isFreeCallMade = true;

        UserProfile userProfile = new UserProfile();
        userProfile.setId(1L);
        userProfile.setIsFreeCallMade(false);

        User user = new User();
        user.setId(userId);
        user.setUserProfile(userProfile);

        UserProfile updatedUserProfile = new UserProfile();
        updatedUserProfile.setId(1L);
        updatedUserProfile.setIsFreeCallMade(isFreeCallMade);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(updatedUserProfile);
        when(modelMapperUtil.map(eq(updatedUserProfile), eq(UserProfileDTO.class))).thenReturn(UserProfileDTO.builder()
                .id(updatedUserProfile.getId())
                .isFreeCallMade(updatedUserProfile.getIsFreeCallMade())
                .build());

        UserProfileDTO updatedProfileDTO = userProfileService.updateUserProfileByUserId(userId, isFreeCallMade);

        assertNotNull(updatedProfileDTO);
        assertEquals(updatedUserProfile.getId(), updatedProfileDTO.getId());
        assertEquals(updatedUserProfile.getIsFreeCallMade(), updatedProfileDTO.getIsFreeCallMade());
        verify(userRepository, times(1)).findById(userId);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    public void testChangeIsFreeCallMade() throws ExceptionGraphql {
        Long userId = 1L;
        Boolean isFreeCallMade = true;

        when(userProfileRepository.updateIsFreeCallMadeByUserId(userId, isFreeCallMade)).thenReturn(1);

        Boolean result = userProfileService.changeIsFreeCallMade(userId, isFreeCallMade);

        assertEquals(true, result);
        verify(userProfileRepository, times(1)).updateIsFreeCallMadeByUserId(userId, isFreeCallMade);
    }

    @Test
    public void testChangeBalanceByUserProfileId() throws ExceptionGraphql {
        Long userId = 1L;
        Double newBalance = 1000.0;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setBalance(500.0);

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setBalance(newBalance);

        UserDTO expectedUserDTO = new UserDTO();
        expectedUserDTO.setId(userId);
        expectedUserDTO.setBalance(newBalance);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(modelMapperUtil.map(updatedUser, UserDTO.class)).thenReturn(expectedUserDTO);

        UserDTO result = userProfileService.changeBalanceByUserProfileId(userId, newBalance);

        assertNotNull(result);
        assertEquals(expectedUserDTO.getId(), result.getId());
        assertEquals(expectedUserDTO.getBalance(), result.getBalance());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(modelMapperUtil, times(1)).map(updatedUser, UserDTO.class);
    }
}
