package com.jobvacancy.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.jobvacancy.domain.JobOffer;
import com.jobvacancy.repository.JobOfferRepository;
import com.jobvacancy.service.MailService;
import com.jobvacancy.service.util.EmailUtil;
import com.jobvacancy.service.util.UrlUtil;
import com.jobvacancy.web.rest.dto.JobApplicationDTO;
import com.jobvacancy.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api")
public class JobApplicationResource {

    private final Logger log = LoggerFactory.getLogger(JobOfferResource.class);

    @Inject
    private JobOfferRepository jobOfferRepository;

    @Inject
    private MailService mailService;

    /**
     * POST  /Application -> Create a new jobOffer.
     */
    @RequestMapping(value = "/Application",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<JobOffer> createJobApplication(@Valid @RequestBody JobApplicationDTO jobApplication) throws URISyntaxException {
        log.debug("REST request to save JobApplication : {}", jobApplication);

        if (EmailUtil.emailIsValid(jobApplication.getEmail())) {
            if (UrlUtil.urlIsValid(jobApplication.getResume())) {
                JobOffer jobOffer = jobOfferRepository.findOne(jobApplication.getOfferId());
                this.mailService.sendApplication(jobApplication.getEmail(), jobApplication.getResume(), jobOffer);

                jobOffer.setApplicationsCount(jobOffer.getApplicationsCount() + 1);
                jobOfferRepository.save(jobOffer);

                return ResponseEntity.accepted()
                    .headers(HeaderUtil.createAlert("Application created and sent offer's owner", "")).body(null);
            } else {
                return ResponseEntity.badRequest()
                    .headers(HeaderUtil.createAlert("Resume is not a valid URL", "")).body(null);
            }
        } else {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createAlert("Email is not valid", "")).body(null);
        }
    }

}
