package hsm.bootproject.SearchFlight.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 받는 생성자
public class chatMessageDto {
    private String content;
    private String sender; // "user" 또는 "AI"
}