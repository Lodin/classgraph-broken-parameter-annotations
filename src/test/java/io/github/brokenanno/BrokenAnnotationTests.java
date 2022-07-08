package io.github.brokenanno;

import static java.util.Map.entry;
import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.MethodParameterInfo;

public class BrokenAnnotationTests {
    @Test
    public void test() throws URISyntaxException {
        var targetDir = Paths
                .get(Objects
                        .requireNonNull(
                                BrokenAnnotation.class.getProtectionDomain()
                                        .getCodeSource().getLocation())
                        .toURI());

        try (var result = new ClassGraph().enableAllInfo()
                .overrideClasspath(List.of(targetDir)).verbose().scan()) {
            var actual = Arrays
                    .stream(result
                            .getClassInfo(
                                    BrokenAnnotation.Dynamic.class.getName())
                            .getDeclaredConstructorInfo().get(0)
                            .getParameterInfo())
                    .collect(Collectors.toMap(MethodParameterInfo::getName,
                            parameter -> Optional
                                    .ofNullable(parameter
                                            .getTypeSignatureOrTypeDescriptor()
                                            .getTypeAnnotationInfo())
                                    .map(list -> list.stream()
                                            .map(AnnotationInfo::getName)
                                            .collect(Collectors.toList()))
                                    .orElse(List.of())));

            var expected = Map.ofEntries(entry("this$0", List.of()),
                    entry("param1", List
                            .of("org.issue.brokenanno.BrokenAnnotation$Foo")),
                    entry("param2", List
                            .of("org.issue.brokenanno.BrokenAnnotation$Bar")));

            assertEquals(expected, actual);
        }
    }
}
