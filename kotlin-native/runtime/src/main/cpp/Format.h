/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#ifndef RUNTIME_FORMAT_H
#define RUNTIME_FORMAT_H

#include <cstdarg>

#include "cpp_support/Span.hpp"

namespace kotlin {

std_support::span<char> FormatToSpan(std_support::span<char> buffer, const char* format, ...) noexcept
        __attribute__((format(printf, 2, 3)));

std_support::span<char> VFormatToSpan(std_support::span<char> buffer, const char* format, std::va_list args) noexcept;

} // namespace kotlin

#endif // RUNTIME_FORMAT_H
