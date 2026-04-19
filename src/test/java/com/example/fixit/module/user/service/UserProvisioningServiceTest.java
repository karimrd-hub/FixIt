package com.example.fixit.module.user.service;

import com.example.fixit.module.user.entity.User;
import com.example.fixit.module.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProvisioningServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserProvisioningService service;

    @Nested
    class WhenUserIsNew {

        @Test
        void savesUserWithCorrectClaims() {
            when(userRepository.existsByKeycloakId("kc-123")).thenReturn(false);

            service.provisionIfAbsent("kc-123", "user@example.com", "johndoe");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            User saved = captor.getValue();

            assertThat(saved.getKeycloakId()).isEqualTo("kc-123");
            assertThat(saved.getEmail()).isEqualTo("user@example.com");
            assertThat(saved.getDisplayName()).isEqualTo("johndoe");
        }
    }

    @Nested
    class WhenUserAlreadyExists {

        @Test
        void skipsSave() {
            when(userRepository.existsByKeycloakId("kc-existing")).thenReturn(true);

            service.provisionIfAbsent("kc-existing", "user@example.com", "johndoe");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class WhenConcurrentRaceOccurs {

        @Test
        void swallowsDataIntegrityViolationFromSimultaneousInsert() {
            when(userRepository.existsByKeycloakId("kc-race")).thenReturn(false);
            when(userRepository.save(any(User.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate key value violates unique constraint"));

            assertThatCode(() -> service.provisionIfAbsent("kc-race", "user@example.com", "johndoe"))
                    .doesNotThrowAnyException();
        }
    }
}
