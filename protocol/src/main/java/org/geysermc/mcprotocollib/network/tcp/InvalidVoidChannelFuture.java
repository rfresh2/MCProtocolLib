package org.geysermc.mcprotocollib.network.tcp;

import com.google.common.base.Suppliers;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class InvalidVoidChannelFuture implements ChannelFuture {

    private static final Supplier<InvalidVoidChannelFuture> memoizedFutureSupplier = Suppliers.memoize(InvalidVoidChannelFuture::new);

    public static InvalidVoidChannelFuture create() {
        return memoizedFutureSupplier.get();
    }

    @Override
    public Channel channel() {
        return null;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public Throwable cause() {
        return null;
    }

    @Override
    public ChannelFuture addListener(final GenericFutureListener<? extends Future<? super Void>> listener) {
        fail();
        return this;
    }

    @Override
    public ChannelFuture addListeners(final GenericFutureListener<? extends Future<? super Void>>... listeners) {
        fail();
        return this;
    }

    @Override
    public ChannelFuture removeListener(final GenericFutureListener<? extends Future<? super Void>> listener) {
        return this;
    }

    @Override
    public ChannelFuture removeListeners(final GenericFutureListener<? extends Future<? super Void>>... listeners) {
        return this;
    }

    @Override
    public ChannelFuture sync() throws InterruptedException {
        fail();
        return this;
    }

    @Override
    public ChannelFuture syncUninterruptibly() {
        return this;
    }

    @Override
    public ChannelFuture await() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return this;
    }

    @Override
    public ChannelFuture awaitUninterruptibly() {
        fail();
        return this;
    }

    @Override
    public boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
        fail();
        return false;
    }

    @Override
    public boolean await(final long timeoutMillis) throws InterruptedException {
        fail();
        return false;
    }

    @Override
    public boolean awaitUninterruptibly(final long timeout, final TimeUnit unit) {
        fail();
        return false;
    }

    @Override
    public boolean awaitUninterruptibly(final long timeoutMillis) {
        fail();
        return false;
    }

    @Override
    public Void getNow() {
        return null;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Void get(final long timeout, @NotNull final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    private static void fail() {
        throw new IllegalStateException("void future");
    }
}
