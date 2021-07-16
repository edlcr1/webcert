/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.webcert.web.service.facade.question.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.question.Question;
import se.inera.intyg.webcert.web.service.arende.ArendeDraftService;
import se.inera.intyg.webcert.web.service.arende.ArendeService;
import se.inera.intyg.webcert.web.service.facade.question.GetQuestionsFacadeService;
import se.inera.intyg.webcert.web.service.facade.question.util.QuestionConverter;

@Service
public class GetQuestionsFacadeServiceImpl implements GetQuestionsFacadeService {

    private final ArendeService arendeService;
    private final ArendeDraftService arendeDraftService;
    private final QuestionConverter questionConverter;

    @Autowired
    public GetQuestionsFacadeServiceImpl(ArendeService arendeService,
        ArendeDraftService arendeDraftService, QuestionConverter questionConverter) {
        this.arendeService = arendeService;
        this.arendeDraftService = arendeDraftService;
        this.questionConverter = questionConverter;
    }

    @Override
    public List<Question> getQuestions(String certificateId) {
        final var arendenInternal = arendeService.getArendenInternal(certificateId);

        final var questionList = arendenInternal.stream()
            .map(questionConverter::convert)
            .collect(Collectors.toList());

        final var questionDraft = getQuestionDraft(certificateId);
        if (questionDraft != null) {
            questionList.add(questionDraft);
        }

        return questionList;
    }

    private Question getQuestionDraft(String certificateId) {
        final var questionDraft = arendeDraftService.getQuestionDraft(certificateId);
        if (questionDraft != null) {
            return questionConverter.convert(questionDraft);
        }
        return null;
    }
}
