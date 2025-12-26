package com.amore.aketer.workflow.batch.config;

import com.amore.aketer.workflow.batch.BatchParams;
import com.amore.aketer.workflow.batch.service.ItemFeatureUpdateService;
import com.amore.aketer.workflow.batch.service.PersonaClusterService;
import com.amore.aketer.workflow.batch.service.PersonaItemMatchService;
import com.amore.aketer.workflow.batch.service.UserFeatureUpdateService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class OfflineBatchJobsConfig {

    // ===== A. UserFeatureUpdate =====
    @Bean
    public Job userFeatureUpdateJob(JobRepository jobRepository, Step userFeatureUpdateStep) {
        return new JobBuilder("UserFeatureUpdateJob", jobRepository)
                .start(userFeatureUpdateStep)
                .build();
    }

    @Bean
    public Step userFeatureUpdateStep(JobRepository jobRepository,
                                      PlatformTransactionManager tx,
                                      UserFeatureUpdateService service) {
        return new StepBuilder("UserFeatureUpdateStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters params = contribution.getStepExecution().getJobParameters();
                    service.run(BatchParams.srchDt(params));
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    // ===== B. PersonaCluster =====
    @Bean
    public Job personaClusterJob(JobRepository jobRepository, Step personaClusterStep) {
        return new JobBuilder("PersonaClusterJob", jobRepository)
                .start(personaClusterStep)
                .build();
    }

    @Bean
    public Step personaClusterStep(JobRepository jobRepository,
                                   PlatformTransactionManager tx,
                                   PersonaClusterService service) {
        return new StepBuilder("PersonaClusterStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    service.run(BatchParams.srchDt(contribution.getStepExecution().getJobParameters()));
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    // ===== C. ItemFeatureUpdate =====
    @Bean
    public Job itemFeatureUpdateJob(JobRepository jobRepository, Step itemFeatureUpdateStep) {
        return new JobBuilder("ItemFeatureUpdateJob", jobRepository)
                .start(itemFeatureUpdateStep)
                .build();
    }

    @Bean
    public Step itemFeatureUpdateStep(JobRepository jobRepository,
                                      PlatformTransactionManager tx,
                                      ItemFeatureUpdateService service) {
        return new StepBuilder("ItemFeatureUpdateStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    service.run(BatchParams.srchDt(contribution.getStepExecution().getJobParameters()));
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    // ===== D. PersonaItemMatch =====
    @Bean
    public Job personaItemMatchJob(JobRepository jobRepository, Step personaItemMatchStep) {
        return new JobBuilder("PersonaItemMatchJob", jobRepository)
                .start(personaItemMatchStep)
                .build();
    }

    @Bean
    public Step personaItemMatchStep(JobRepository jobRepository,
                                     PlatformTransactionManager tx,
                                     PersonaItemMatchService service) {
        return new StepBuilder("PersonaItemMatchStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    service.run(BatchParams.srchDt(contribution.getStepExecution().getJobParameters()));
                    return RepeatStatus.FINISHED;
                }, tx)
                .build();
    }

    @Bean
    public Job offlineMasterJob(JobRepository jobRepository,
                                Step userFeatureUpdateStep,
                                Step personaClusterStep,
                                Step itemFeatureUpdateStep,
                                Step personaItemMatchStep) {
        return new JobBuilder("OfflineMasterJob", jobRepository)
                .start(userFeatureUpdateStep)
                .next(personaClusterStep)
                .next(itemFeatureUpdateStep)
                .next(personaItemMatchStep)
                .build();
    }
}
