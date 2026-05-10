const API_BASE = "/api";

export class ApiError extends Error {
  constructor(message, status, details, fieldErrors = []) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details;
    this.fieldErrors = fieldErrors;
  }
}

function getStoredToken() {
  return localStorage.getItem("hotel_token") || "";
}

function readErrorMessages(data) {
  if (!data || typeof data === "string") {
    return [];
  }

  if (Array.isArray(data.errors)) {
    return data.errors.map((error) => String(error));
  }

  if (data.errors && typeof data.errors === "object") {
    return Object.entries(data.errors).map(([field, message]) => `${field}: ${message}`);
  }

  if (Array.isArray(data.details)) {
    return data.details.map((detail) => String(detail));
  }

  if (data.details && typeof data.details === "object") {
    return Object.entries(data.details).map(([field, message]) => `${field}: ${message}`);
  }

  return [];
}

function readErrorMessage(data, status) {
  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data?.message) {
    return data.message;
  }

  if (data?.error) {
    return data.error;
  }

  return status ? `Request failed with status ${status}` : "Request failed";
}

export function formatApiError(error) {
  if (error instanceof ApiError) {
    return {
      title: error.message,
      status: error.status,
      items: error.fieldErrors,
    };
  }

  if (error instanceof TypeError) {
    return {
      title: "Could not reach the backend. Check that the Spring Boot server is running on HTTPS.",
      status: "",
      items: [],
    };
  }

  return {
    title: error?.message || "Something went wrong",
    status: "",
    items: [],
  };
}

export async function apiRequest(path, options = {}, token = getStoredToken()) {
  const { auth = true, ...fetchOptions } = options;
  const authToken = auth === false ? "" : token || getStoredToken();
  const headers = {
    ...(fetchOptions.body ? { "Content-Type": "application/json" } : {}),
    ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
    ...fetchOptions.headers,
  };

  const response = await fetch(`${API_BASE}${path}`, {
    ...fetchOptions,
    headers,
  });

  const contentType = response.headers.get("content-type") || "";
  const data = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    throw new ApiError(readErrorMessage(data, response.status), response.status, data, readErrorMessages(data));
  }

  return data;
}

export function pageItems(page) {
  return page?.content || [];
}

export function totalItems(page) {
  return page?.totalElements ?? page?.totalElementsCount ?? pageItems(page).length;
}
