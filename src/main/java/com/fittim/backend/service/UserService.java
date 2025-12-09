package com.fittim.backend.service;

import com.fittim.backend.dto.UserProfileDto;
import com.fittim.backend.entity.User;
import com.fittim.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserRepository userRepository;

        @Transactional(readOnly = true)
        public UserProfileDto getMyProfile(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                return UserProfileDto.from(user);
        }

        @Transactional
        public UserProfileDto updateProfile(String email, com.fittim.backend.dto.UserProfileUpdateDto dto) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                user.updateProfile(
                                dto.height(),
                                dto.weight(),
                                dto.bodyType(),
                                dto.gender(),
                                dto.preferredStyles(),
                                dto.preferredMalls());

                return UserProfileDto.from(user);
        }

        @Transactional
        public UserProfileDto patchProfile(String email, com.fittim.backend.dto.UserProfileUpdateDto dto) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                user.patchProfile(
                                dto.height(),
                                dto.weight(),
                                dto.bodyType(),
                                dto.gender(),
                                dto.preferredStyles(),
                                dto.preferredMalls());

                return UserProfileDto.from(user);
        }
}
