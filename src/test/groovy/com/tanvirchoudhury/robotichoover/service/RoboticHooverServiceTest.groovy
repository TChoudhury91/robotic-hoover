package com.tanvirchoudhury.robotichoover.service

import com.tanvirchoudhury.robotichoover.model.db.Coordinates
import com.tanvirchoudhury.robotichoover.repository.UncleanEnvironmentRepository
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static com.tanvirchoudhury.robotichoover.fixtures.CurrentCleanStatusFixtures.aCurrentClean
import static com.tanvirchoudhury.robotichoover.fixtures.CurrentCleanStatusFixtures.aCurrentCleanWithAGivenPatch
import static com.tanvirchoudhury.robotichoover.fixtures.UncleanEnvironmentFixtures.uncleanEnvironmentFromTestExample

class RoboticHooverServiceTest extends Specification {

    @Subject
    RoboticHooverService subject

    UncleanEnvironmentRepository uncleanEnvironmentRepository

    def setup() {
        uncleanEnvironmentRepository = Mock()
        subject = new RoboticHooverService(uncleanEnvironmentRepository)
    }

    def "Cleaning process cleans environment"() {

        when: "Cleaning process is started"
        def result = subject.startCleaningProcess(uncleanEnvironmentFromTestExample())

        then: "Returned result of the cleaning process is as expected"
        result.coords.x == 1
        result.coords.y == 3

        result.patches == 1
    }

    def "Unclean Environment is converted to a CurrentCleanStatus"() {

        given: "Unclean Environment"
        def uncleanEnv = uncleanEnvironmentFromTestExample()

        when: "Converting a Unclean Environment"
        def result = subject.createCurrentCleanStatus(uncleanEnv)

        then: "Returns the expected Current Clean Status"
        result.roomSize.x == uncleanEnv.roomSize.getX()
        result.roomSize.y == uncleanEnv.roomSize.getY()
        result.currentCoords.x == uncleanEnv.coords.getX()
        result.currentCoords.y == uncleanEnv.coords.getY()

        def patchesCoordinates = result.patches
        def uncleanEnvPatches = uncleanEnv.patches.coordinates
        patchesCoordinates.size() == uncleanEnvPatches.size()
        for (int c = 0; c < patchesCoordinates.size(); c++) {
            assert patchesCoordinates.get(c).x == uncleanEnvPatches.get(c).getX()
            assert patchesCoordinates.get(c).y == uncleanEnvPatches.get(c).getY()
        }
    }

    @Unroll
    def "Cardinal directions are converted to the correct coordinates"() {

        when:
        def result = subject.cardinalDirectionToCoords(cardinalCoords as char)

        then:
        result.x == coordinates[0]
        result.y == coordinates[1]

        where:
        cardinalCoords | coordinates
        'N'            | [0, 1]
        'E'            | [1, 0]
        'S'            | [0, -1]
        'W'            | [-1, 0]
    }

    @Unroll
    def "Given a cardinal direction, it will update the CurrentCleanProcess accordingly"() {

        given:
        def currentClean = aCurrentClean()

        when:
        def result = subject.moveHoover(aCurrentClean(), cardinalCoords as char)

        then:
        result.roomSize.x == currentClean.roomSize.x
        result.roomSize.y == currentClean.roomSize.y

        result.currentCoords.x == currentClean.currentCoords.x + expectedCoordinates[0]
        result.currentCoords.y == currentClean.currentCoords.y + expectedCoordinates[1]

        where:
        cardinalCoords | expectedCoordinates
        'N'            | [0, 1]
        'E'            | [1, 0]
        'S'            | [0, -1]
        'W'            | [-1, 0]
    }

    def "When a hoover goes over a patch, that patch is removed from CurrentCleanProcess"() {

        given:
        def currentClean = aCurrentCleanWithAGivenPatch(new Coordinates(3, 1))

        when:
        def result = subject.moveHoover(currentClean, 'E' as char)

        then:
        result.patches.isEmpty()
    }
}
