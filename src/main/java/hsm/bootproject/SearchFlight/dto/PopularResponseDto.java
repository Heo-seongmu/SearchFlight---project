package hsm.bootproject.SearchFlight.dto;

import java.util.List;
import java.util.stream.Collectors;

import hsm.bootproject.SearchFlight.domain.Activity;
import hsm.bootproject.SearchFlight.domain.popular;
import lombok.Data;

@Data
public class PopularResponseDto {
    private String desc; // JS의 data.desc에 매핑될 변수
    private List<ActivityDto> activities; // JS의 data.activities에 매핑될 리스트

    // 엔티티 -> DTO 변환 생성자
    public PopularResponseDto(popular popular) {
        this.desc = popular.getDescription(); 

        this.activities = popular.getActivities().stream()
                .map(ActivityDto::new)
                .collect(Collectors.toList());
    }

    @Data
    public static class ActivityDto {
        private String title;
        private String img;  
        private String text; 

        public ActivityDto(Activity activity) {
            this.title = activity.getTitle();
            this.img = activity.getImageUrl(); 
            this.text = activity.getText();
        }
    }
}