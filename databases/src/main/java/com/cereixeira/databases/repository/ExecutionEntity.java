package com.cereixeira.databases.repository;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name="EXECUTION"/*, indexes = @Index(name = "IND_NAME", columnList = "NAME")*/)
public class ExecutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="ID", unique = true)
    private Long id;

    @Column(name="NAME", nullable = false, unique = true)
    private String name;

    @Column(name="INPUT_PATH", nullable = false)
    private String inputPath;

    @Column(name="DATE")
    @UpdateTimestamp
    private Date date;

}
