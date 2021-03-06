package ${project_package}

import griffon.core.artifact.GriffonController
import griffon.inject.MVCMember
import griffon.metadata.ArtifactProviderFor
import griffon.transform.Threading
import javax.annotation.Nonnull

@ArtifactProviderFor(GriffonController)
class ${project_class_name}Controller {
    @MVCMember @Nonnull
    ${project_class_name}Model model

    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    void click() {
        model.clickCount++
    }
}