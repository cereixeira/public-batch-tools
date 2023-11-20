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
@Table(name="ENTRY")
public class EntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID", unique = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_BLOCK")
    private BlockEntity block ;

    @Column(name="UNIQUE_REF", nullable = false, unique = true)
    private String uniqueRef;

//    @Column(name="UNIQUE_HASH", nullable = false, unique = true)
//    private String uniqueHash;

    @Column(name="NODE_REF")
    private String nodeRef;

    @Column(name="ERROR", length = 2000)
    private String error;

    @Column(name="INPUT_DATA", length = 2000)
    private String inputData;

    @Column(name="OUTPUT_DATA", length = 2000)
    private String outputData;

    @Column(name="DATE_INI")
    private Date dateIni;

    @Column(name="DATE_END")
    private Date dateEnd;

    @Column(name="COMPLETED", nullable = false)
    private boolean completed;

    @Column(name="WRITING_DURATION")
    private Float writingDuration;

    @Column(name="FILE_SIZE")
    private Float fileSize;
}
