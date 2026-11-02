package bf.hydro.stations.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import bf.hydro.stations.service.StationService.ConflitException;
import bf.hydro.stations.service.StationService.IntrouvableException;
import jakarta.validation.ConstraintViolationException;

/** Traduit les erreurs métier en réponses HTTP lisibles (format RFC 9457). */
@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler(IntrouvableException.class)
    ProblemDetail introuvable(IntrouvableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflitException.class)
    ProblemDetail conflit(ConflitException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail requeteInvalide(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class,
            ConstraintViolationException.class})
    ProblemDetail validation(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Données invalides : " + e.getMessage());
    }
}
