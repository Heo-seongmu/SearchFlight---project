package hsm.bootproject.SearchFlight.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "passengers")
@Getter
@Setter
public class Passenger {


	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(nullable = false)
	    private String firstName;

	    @Column(nullable = false)
	    private String lastName;

	    @Column(nullable = false)
	    private String gender; // "MALE", "FEMALE"

	    @Column(nullable = false)
	    private String birthDate; // "YYYY-MM-DD"

	    @Column(nullable = false)
	    private String passengerType; // "ADULT", "CHILD", "INFANT"

	    // (국제선용)
	    private String passportNumber;
	    private String passportIssuingCountry;
	    private String passportExpiryDate;

	    // ⭐️ 이 탑승객이 어떤 예약에 속해있는지 연결
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "booking_id", nullable = false)
	    private Booking booking;
	
}
