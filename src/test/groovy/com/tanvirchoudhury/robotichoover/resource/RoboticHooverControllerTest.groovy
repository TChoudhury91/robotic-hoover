package com.tanvirchoudhury.robotichoover.resource

import com.tanvirchoudhury.robotichoover.model.dto.CleanEnvironmentResultDto
import com.tanvirchoudhury.robotichoover.service.RoboticHooverService
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Subject

import static com.tanvirchoudhury.robotichoover.fixtures.UncleanEnvironmentDtoFixtures.aUncleanEnvironmentDto

class RoboticHooverControllerTest extends Specification {

    @Subject
    RoboticHooverController subject

    RoboticHooverService hooverCleanService

    def setup() {
        hooverCleanService = Mock(RoboticHooverService)
        subject = new RoboticHooverController(hooverCleanService)
    }

    def "A received UncleanEnvironmentDto will start and return the result of the cleaning process"() {

        given: "A unclean environment"
        def uncleanEnvDto = aUncleanEnvironmentDto()

        and: "A clean environment"
        def cleanEnvDto = new CleanEnvironmentResultDto([1, 2], 3)

        when: "A clean is requested"
        def result = subject.clean(uncleanEnvDto)

        then: "The cleaning process is started"
        1 * hooverCleanService.cleanEnvironment(uncleanEnvDto) >> cleanEnvDto

        and: "The result of the cleaning is returned"
        result.statusCode == HttpStatus.OK
        result.getBody() == cleanEnvDto
    }
}