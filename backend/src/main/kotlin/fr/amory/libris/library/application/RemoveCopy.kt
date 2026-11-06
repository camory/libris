package fr.amory.libris.library.application

import fr.amory.libris.library.application.RemoveCopyResult.NoSuchCopy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class RemoveCopy {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId, copyId: CopyId): RemoveCopyResult =
    NoSuchCopy
}
