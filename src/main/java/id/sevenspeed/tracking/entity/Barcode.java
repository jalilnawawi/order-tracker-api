package id.sevenspeed.tracking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "barcodes")
@Getter
@Setter
@NoArgsConstructor
public class Barcode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private OrderBatch batch;

    @Column(name = "barcode_type", nullable = false, length = 20)
    private String barcodeType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "printed_at")
    private OffsetDateTime printedAt;

    @Column(length = 255)
    private String notes;
}