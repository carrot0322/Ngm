package me.coolmint.ngm.util.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import me.coolmint.ngm.features.settings.Setting;
import net.minecraft.entity.Entity;

public class EntityFilterList
{
    private final List<EntityFilter> entityFilters;

    public EntityFilterList(EntityFilter... filters)
    {
        this(Arrays.asList(filters));
    }

    public EntityFilterList(List<EntityFilter> filters)
    {
        entityFilters = Collections.unmodifiableList(filters);
    }

    public final void forEach(Consumer<? super Setting> action)
    {
        entityFilters.stream().map(EntityFilter::getSetting).forEach(action);
    }

    public final <T extends Entity> Stream<T> applyTo(Stream<T> stream)
    {
        for(EntityFilter filter : entityFilters)
        {
            if(!filter.isFilterEnabled())
                continue;

            stream = stream.filter(filter);
        }

        return stream;
    }

    public final boolean testOne(Entity entity)
    {
        for(EntityFilter filter : entityFilters)
            if(filter.isFilterEnabled() && !filter.test(entity))
                return false;

        return true;
    }

    public static EntityFilterList genericCombat()
    {
        return new EntityFilterList(

        );
    }

    public static interface EntityFilter extends Predicate<Entity>
    {
        public boolean isFilterEnabled();

        public Setting getSetting();
    }
}
