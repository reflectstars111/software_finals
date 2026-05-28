package com.personality.radar.domain;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Question question;

    @Column(nullable = false, length = 20)
    private String label;

    @Column(nullable = false, length = 180)
    private String content;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @ElementCollection
    @CollectionTable(name = "question_option_weights", joinColumns = @JoinColumn(name = "option_id"))
    @MapKeyColumn(name = "dimension")
    @Column(name = "weight_value")
    private Map<String, Integer> weights = new HashMap<>();

    public Long getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Map<String, Integer> getWeights() {
        return weights;
    }

    public void setWeights(Map<String, Integer> weights) {
        this.weights = weights;
    }
}

