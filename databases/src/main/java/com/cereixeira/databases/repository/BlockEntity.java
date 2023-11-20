package com.cereixeira.databases.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name="BLOCK",
        indexes = {
            @Index(name = "INDEX_EXECUTION_COMPLETED", columnList = "ID_EXECUTION, COMPLETED"),
            @Index(name = "INDEX_EXECUTION_FILEPATH", columnList = "ID_EXECUTION, REF_PATH")
})
public class BlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID", unique = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_EXECUTION")
    private ExecutionEntity execution;

    @Column(name="REF_PATH", nullable = false)
    private String refPath;

    @Column(name="DATE_INI")
    private Date dateIni;

    @Column(name="DATE_END")
    private Date dateEnd;

    @Column(name="VALID_ENTRIES")
    private int validEntries;

    @Column(name="COMPLETED_ENTRIES")
    private int completedEntries;

    @Column(name="COMPLETED")
    private boolean completed;

    @Column(name="PROCESSING_DURATION")
    private Float processingDuration;

    @Column(name="WRITING_DURATION")
    private Float writingDuration;

    @Column(name="FILE_SIZES")
    private Float fileSizes;

}
