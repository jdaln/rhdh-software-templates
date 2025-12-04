package ${{values.javaPackageName}};

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class NameRepository implements PanacheRepository<Name> {
    
    private final Random random = new Random();
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    public Name getRandomName() {
        try {
            List<Name> names = listAll();
            if (names.isEmpty()) {
                return null;
            }
            return names.get(random.nextInt(names.size()));
        } catch (Exception e) {
            // Handle database connection errors gracefully
            return null;
        }
    }
    
    public Name getNextName() {
        try {
            List<Name> names = listAll();
            if (names.isEmpty()) {
                return null;
            }
            int index = currentIndex.getAndIncrement() % names.size();
            return names.get(index);
        } catch (Exception e) {
            // Handle database connection errors gracefully
            return null;
        }
    }
}

