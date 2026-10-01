package fr.becpg.repo.survey.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

import fr.becpg.repo.survey.data.SurveyQuestion;

public class SurveyServiceImplTest {

    private static final NodeRef ANSWER_NODE = new NodeRef("workspace://SpacesStore/answer");

    private static final NodeRef UNKNOWN_NODE = new NodeRef("workspace://SpacesStore/unknown");

    private SurveyQuestion followUpQuestion;

    private SurveyQuestion answer;

    private Map<NodeRef, SurveyQuestion> surveyQuestionByNodeRef;

    @Before
    public void setUp() {
        followUpQuestion = question("Follow-up");
        answer = question("Yes");
        answer.setNextQuestions(new ArrayList<>(List.of(followUpQuestion)));
        surveyQuestionByNodeRef = new HashMap<>();
        surveyQuestionByNodeRef.put(ANSWER_NODE, answer);
    }

    @Test
    public void aRowWithNoChoiceLeadsNowhere() {
        assertFalse(SurveyServiceImpl.leadsTo(null, followUpQuestion, surveyQuestionByNodeRef));
    }

    @Test
    public void anAnswerListingTheQuestionLeadsToIt() {
        assertTrue(SurveyServiceImpl.leadsTo(List.of(ANSWER_NODE), followUpQuestion, surveyQuestionByNodeRef));
    }

    @Test
    public void anAnswerMissingFromTheQuestionnaireLeadsNowhere() {
        assertFalse(SurveyServiceImpl.leadsTo(List.of(UNKNOWN_NODE), followUpQuestion, surveyQuestionByNodeRef));
    }

    @Test
    public void anAnswerWithNoNextQuestionLeadsNowhere() {
        answer.setNextQuestions(null);

        assertFalse(SurveyServiceImpl.leadsTo(List.of(ANSWER_NODE), followUpQuestion, surveyQuestionByNodeRef));
    }

    @Test
    public void anAnswerLeadingElsewhereDoesNotLeadToTheQuestion() {
        assertFalse(SurveyServiceImpl.leadsTo(List.of(ANSWER_NODE), question("Other"), surveyQuestionByNodeRef));
    }

    private static SurveyQuestion question(String label) {
        SurveyQuestion question = new SurveyQuestion();
        question.setLabel(label);
        return question;
    }
}
