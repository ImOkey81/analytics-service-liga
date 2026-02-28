package com.univerliga.analytics.service.mock;

import com.univerliga.analytics.model.FeedbackRecord;
import com.univerliga.analytics.model.PersonRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class MockDataFactory {

    public List<PersonRecord> people() {
        return List.of(
                new PersonRecord("p_1", "Ivan P.", "d_1", "t_1"),
                new PersonRecord("p_2", "Olga K.", "d_1", "t_1"),
                new PersonRecord("p_3", "Nikita V.", "d_1", "t_2"),
                new PersonRecord("p_4", "Mira S.", "d_1", "t_2"),
                new PersonRecord("p_5", "Artem L.", "d_2", "t_3"),
                new PersonRecord("p_6", "Anna Z.", "d_2", "t_3"),
                new PersonRecord("p_7", "Pavel M.", "d_2", "t_4"),
                new PersonRecord("p_8", "Elena D.", "d_2", "t_4"),
                new PersonRecord("p_9", "Roman T.", "d_3", "t_5"),
                new PersonRecord("p_10", "Daria F.", "d_3", "t_5")
        );
    }

    public List<FeedbackRecord> feedbacks() {
        String[][] categories = {
                {"cat_1", "Performance", "sub_1", "Quality"},
                {"cat_1", "Performance", "sub_2", "Delivery"},
                {"cat_2", "Culture", "sub_3", "Communication"},
                {"cat_2", "Culture", "sub_4", "Teamwork"},
                {"cat_3", "Growth", "sub_5", "Mentoring"},
                {"cat_3", "Growth", "sub_6", "Learning"}
        };

        List<PersonRecord> persons = people();
        List<FeedbackRecord> out = new ArrayList<>();
        LocalDate base = LocalDate.of(2026, 1, 1);
        for (int i = 1; i <= 80; i++) {
            PersonRecord author = persons.get((i * 3) % persons.size());
            PersonRecord target = persons.get((i * 5 + 1) % persons.size());
            String[] c = categories[i % categories.length];
            int rating = (i % 5) + 1;
            out.add(new FeedbackRecord(
                    "f_" + i,
                    author.id(),
                    target.id(),
                    target.displayName(),
                    target.departmentId(),
                    target.teamId(),
                    c[0],
                    c[1],
                    c[2],
                    c[3],
                    rating,
                    base.plusDays(i)
            ));
        }
        return out;
    }
}
