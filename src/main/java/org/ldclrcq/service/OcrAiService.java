package org.ldclrcq.service;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import org.ldclrcq.dto.OcrResult;

@RegisterAiService
public interface OcrAiService {

    @UserMessage("You are an OCR and information extraction assistant. " +
            "You will be given a single cookbook index page as an embedded base64 image data URL. " +
            "Extract a list of recipes with their ingredient keyword and page number. " +
            "Respond ONLY with strict JSON using this schema: {\n" +
            "  \"recipes\": [\n" +
            "    { \"ingredient\": string, \"recipeName\": string, \"pageNumber\": number, \"confidence\": number }\n" +
            "  ]\n" +
            "}.\n" +
            "Notes: If a value is ambiguous, make your best guess and lower confidence. " +
            "Confidence is a float between 0 and 1. No markdown, no extra text.\n\n")
    OcrResult extract(Image image);
}
