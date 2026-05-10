import { useEffect, useMemo, useState } from "react";
import {
  BedDouble,
  Building2,
  CalendarCheck,
  Check,
  CircleDollarSign,
  DoorOpen,
  Hotel,
  Loader2,
  LogIn,
  LogOut,
  MapPin,
  Search,
  Shield,
  Sparkles,
  UserPlus,
  X,
} from "lucide-react";
import { apiRequest, formatApiError, pageItems, totalItems } from "./api";

const today = new Date().toISOString().slice(0, 10);
const tomorrow = new Date(Date.now() + 86400000).toISOString().slice(0, 10);

function Field({ label, icon: Icon, children }) {
  return (
    <label className="block">
      <span className="mb-2 flex items-center gap-2 text-sm font-semibold text-slate-700">
        {Icon ? <Icon className="h-4 w-4 text-emerald-700" /> : null}
        {label}
      </span>
      {children}
    </label>
  );
}

function TextInput(props) {
  return (
    <input
      {...props}
      className="w-full rounded-md border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-emerald-600 focus:ring-4 focus:ring-emerald-100"
    />
  );
}

function SelectInput(props) {
  return (
    <select
      {...props}
      className="w-full rounded-md border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-emerald-600 focus:ring-4 focus:ring-emerald-100"
    />
  );
}

function Button({ children, icon: Icon, variant = "primary", busy = false, className = "", ...props }) {
  const variants = {
    primary: "bg-emerald-700 text-white hover:bg-emerald-800 focus:ring-emerald-200",
    secondary: "bg-slate-900 text-white hover:bg-slate-800 focus:ring-slate-200",
    ghost: "bg-white text-slate-800 ring-1 ring-slate-200 hover:bg-slate-50 focus:ring-slate-300",
    danger: "bg-rose-600 text-white hover:bg-rose-700 focus:ring-rose-200",
  };

  return (
    <button
      {...props}
      disabled={props.disabled || busy}
      className={`inline-flex min-h-10 items-center justify-center gap-2 rounded-md px-4 py-2 text-sm font-semibold shadow-sm outline-none transition focus:ring-4 disabled:cursor-not-allowed disabled:opacity-60 ${variants[variant]} ${className}`}
    >
      {busy ? <Loader2 className="h-4 w-4 animate-spin" /> : Icon ? <Icon className="h-4 w-4" /> : null}
      {children}
    </button>
  );
}

function EmptyState({ title, text }) {
  return (
    <div className="rounded-md border border-dashed border-slate-300 bg-white/70 p-6 text-center">
      <p className="font-semibold text-slate-800">{title}</p>
      <p className="mt-1 text-sm text-slate-500">{text}</p>
    </div>
  );
}

function Metric({ label, value, icon: Icon, tone }) {
  return (
    <div className="rounded-md border border-white/70 bg-white/85 p-4 shadow-sm">
      <div className="flex items-center justify-between gap-3">
        <span className="text-sm font-medium text-slate-500">{label}</span>
        <span className={`rounded-md p-2 ${tone}`}>
          <Icon className="h-4 w-4" />
        </span>
      </div>
      <p className="mt-3 text-2xl font-bold text-slate-950">{value}</p>
    </div>
  );
}

export default function App() {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem("hotel_token") || "";
    const role = localStorage.getItem("hotel_role") || "";
    return { token, role };
  });
  const [login, setLogin] = useState({ email: "", password: "" });
  const [signupForm, setSignupForm] = useState({ email: "", password: "", role: "STAFF" });
  const [activeHotel, setActiveHotel] = useState(null);
  const [hotelsPage, setHotelsPage] = useState(null);
  const [roomsPage, setRoomsPage] = useState(null);
  const [bookingsPage, setBookingsPage] = useState(null);
  const [customer, setCustomer] = useState(null);
  const [notice, setNotice] = useState("");
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState("");

  const [hotelFilters, setHotelFilters] = useState({
    city: "",
    checkIn: today,
    checkOut: tomorrow,
  });
  const [roomFilters, setRoomFilters] = useState({
    minPrice: "",
    maxPrice: "",
    checkIn: today,
    checkOut: tomorrow,
  });
  const [customerForm, setCustomerForm] = useState({
    name: "",
    email: "",
    phone: "",
  });
  const [bookingForm, setBookingForm] = useState({
    customerId: "",
    hotelId: "",
    roomId: "",
    checkInDate: today,
    checkOutDate: tomorrow,
  });
  const [bookingFilters, setBookingFilters] = useState({
    status: "",
    customerId: "",
    hotelId: "",
  });

  const isAuthed = Boolean(auth.token);
  const isAdmin = auth.role === "ADMIN";
  const hotels = pageItems(hotelsPage);
  const rooms = pageItems(roomsPage);
  const bookings = pageItems(bookingsPage);

  const selectedRoom = useMemo(
    () => rooms.find((room) => String(room.id) === String(bookingForm.roomId)),
    [rooms, bookingForm.roomId]
  );

  function saveAuth(nextAuth) {
    setAuth(nextAuth);
    localStorage.setItem("hotel_token", nextAuth.token);
    localStorage.setItem("hotel_role", nextAuth.role);
  }

  function clearMessages() {
    setError(null);
    setNotice("");
  }

  async function run(action, fn) {
    clearMessages();
    setBusy(action);
    try {
      await fn();
    } catch (err) {
      const displayError = formatApiError(err);
      setError(displayError);
      if (displayError.status === 401) {
        handleLogout();
      }
    } finally {
      setBusy("");
    }
  }

  async function handleLogin(event) {
    event.preventDefault();
    run("login", async () => {
      const data = await apiRequest("/auth/login", {
        method: "POST",
        body: JSON.stringify(login),
        auth: false,
      });
      saveAuth({ token: data.token, role: data.role });
      setNotice(`Signed in as ${data.role}`);
    });
  }

  async function handleSignup(event) {
    event.preventDefault();
    run("signup", async () => {
      const message = await apiRequest("/auth/register", {
        method: "POST",
        body: JSON.stringify(signupForm),
        auth: false,
      });
      setNotice(typeof message === "string" ? message : "User created successfully, Please login to continue");
      setSignupForm({ email: "", password: "", role: "STAFF" });
    });
  }

  function handleLogout() {
    localStorage.removeItem("hotel_token");
    localStorage.removeItem("hotel_role");
    setAuth({ token: "", role: "" });
    setHotelsPage(null);
    setRoomsPage(null);
    setBookingsPage(null);
    setActiveHotel(null);
    setCustomer(null);
  }

  async function searchHotels(event) {
    event?.preventDefault();
    run("hotels", async () => {
      const params = new URLSearchParams({ page: "0", size: "12" });
      if (hotelFilters.city) params.set("city", hotelFilters.city);
      if (hotelFilters.checkIn) params.set("checkIn", hotelFilters.checkIn);
      if (hotelFilters.checkOut) params.set("checkOut", hotelFilters.checkOut);
      const data = await apiRequest(`/hotels/available?${params}`);
      setHotelsPage(data);
      setRoomsPage(null);
      setActiveHotel(null);
    });
  }

  async function searchRooms(hotel) {
    const currentHotel = hotel || activeHotel;
    if (!currentHotel) return;
    run("rooms", async () => {
      setActiveHotel(currentHotel);
      const params = new URLSearchParams({
        page: "0",
        size: "10",
        sortBy: "price",
        sortDir: "asc",
      });
      Object.entries(roomFilters).forEach(([key, value]) => {
        if (value) params.set(key, value);
      });
      const data = await apiRequest(`/hotels/${currentHotel.id}/rooms?${params}`);
      setRoomsPage(data);
      setBookingForm((prev) => ({ ...prev, hotelId: currentHotel.id }));
    });
  }

  async function createCustomer(event) {
    event.preventDefault();
    run("customer", async () => {
      const data = await apiRequest(
        "/customers",
        { method: "POST", body: JSON.stringify(customerForm) }
      );
      const nextCustomer = data.data || data;
      setCustomer(nextCustomer);
      setBookingForm((prev) => ({ ...prev, customerId: nextCustomer.id || "" }));
      setNotice(data.message || "Customer ready");
    });
  }

  async function createBooking(event) {
    event.preventDefault();
    run("booking", async () => {
      const payload = {
        ...bookingForm,
        customerId: Number(bookingForm.customerId),
        hotelId: Number(bookingForm.hotelId),
        roomId: Number(bookingForm.roomId),
      };
      const booking = await apiRequest(
        "/bookings",
        { method: "POST", body: JSON.stringify(payload) }
      );
      setNotice(`Booking #${booking.bookingId} created`);
      await loadBookings();
    });
  }

  async function loadBookings(event) {
    event?.preventDefault();
    await run("bookings", async () => {
      const params = new URLSearchParams({ page: "0", size: "10", sortBy: "id", sortDir: "desc" });
      Object.entries(bookingFilters).forEach(([key, value]) => {
        if (value) params.set(key, value);
      });
      const data = await apiRequest(`/bookings?${params}`);
      setBookingsPage(data);
    });
  }

  async function updateBooking(bookingId, action) {
    run(`${action}-${bookingId}`, async () => {
      const updated = await apiRequest(`/bookings/${bookingId}/${action}`, { method: "PUT" });
      setNotice(`Booking #${updated.bookingId} ${updated.status.toLowerCase()}`);
      await loadBookings();
    });
  }

  useEffect(() => {
    if (isAuthed && isAdmin && !hotelsPage) {
      searchHotels();
    }
    if (isAuthed && !bookingsPage) {
      loadBookings();
    }
  }, [isAuthed]);

  return (
    <div className="min-h-screen bg-[#f5f7f4]">
      <header className="relative overflow-hidden border-b border-emerald-900/10 bg-slate-950 text-white">
        <img
          src="https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1800&q=80"
          alt=""
          className="absolute inset-0 h-full w-full object-cover opacity-35"
        />
        <div className="absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-950/86 to-emerald-950/55" />
        <div className="relative mx-auto flex max-w-7xl flex-col gap-8 px-4 py-8 sm:px-6 lg:px-8">
          <nav className="flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <span className="rounded-md bg-white p-2 text-emerald-800 shadow-sm">
                <Hotel className="h-6 w-6" />
              </span>
              <div>
                <p className="text-xl font-bold tracking-normal">Hotel Booking System</p>
                <p className="text-sm text-emerald-50/80">Operations dashboard</p>
              </div>
            </div>
            {isAuthed ? (
              <div className="flex flex-wrap items-center gap-3">
                <span className="inline-flex items-center gap-2 rounded-md bg-white/12 px-3 py-2 text-sm font-semibold ring-1 ring-white/20">
                  <Shield className="h-4 w-4" />
                  {auth.role}
                </span>
                <Button icon={LogOut} variant="ghost" onClick={handleLogout}>
                  Sign out
                </Button>
              </div>
            ) : null}
          </nav>

          <section className="grid gap-6 lg:grid-cols-[1.25fr_0.75fr] lg:items-end">
            <div className="max-w-2xl">
              <h1 className="text-4xl font-bold tracking-normal text-white sm:text-5xl">
                Book rooms, manage guests, and track stays
              </h1>
              <p className="mt-4 max-w-xl text-base leading-7 text-slate-100">
                Search availability, prepare customers, create reservations, and move bookings through confirmation or cancellation.
              </p>
            </div>
            <div className="grid grid-cols-3 gap-3">
              <Metric label="Hotels" value={hotels.length || "-"} icon={Building2} tone="bg-emerald-50 text-emerald-700" />
              <Metric label="Rooms" value={rooms.length || "-"} icon={BedDouble} tone="bg-sky-50 text-sky-700" />
              <Metric label="Bookings" value={bookings.length || "-"} icon={CalendarCheck} tone="bg-amber-50 text-amber-700" />
            </div>
          </section>
        </div>
      </header>

      <main className="mx-auto grid max-w-7xl gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[360px_1fr] lg:px-8">
        <aside className="space-y-6">
          {!isAuthed ? (
            <>
              <form onSubmit={handleLogin} className="rounded-md bg-white p-5 shadow-soft ring-1 ring-slate-200">
                <h2 className="flex items-center gap-2 text-lg font-bold text-slate-950">
                  <LogIn className="h-5 w-5 text-emerald-700" />
                  Staff Login
                </h2>
                <div className="mt-5 space-y-4">
                  <Field label="Email">
                    <TextInput type="text" value={login.email} onChange={(e) => setLogin({ ...login, email: e.target.value })} />
                  </Field>
                  <Field label="Password">
                    <TextInput type="password" value={login.password} onChange={(e) => setLogin({ ...login, password: e.target.value })} />
                  </Field>
                  <Button icon={LogIn} busy={busy === "login"} className="w-full">
                    Sign in
                  </Button>
                </div>
              </form>

              <form onSubmit={handleSignup} className="rounded-md bg-white p-5 shadow-soft ring-1 ring-slate-200">
                <h2 className="flex items-center gap-2 text-lg font-bold text-slate-950">
                  <UserPlus className="h-5 w-5 text-emerald-700" />
                  Signup
                </h2>
                <div className="mt-5 space-y-4">
                  <Field label="Email">
                    <TextInput type="text" value={signupForm.email} onChange={(e) => setSignupForm({ ...signupForm, email: e.target.value })} />
                  </Field>
                  <Field label="Password">
                    <TextInput type="password" value={signupForm.password} onChange={(e) => setSignupForm({ ...signupForm, password: e.target.value })} />
                  </Field>
                  <Field label="Role">
                    <SelectInput value={signupForm.role} onChange={(e) => setSignupForm({ ...signupForm, role: e.target.value })}>
                      <option value="STAFF">STAFF</option>
                      <option value="ADMIN">ADMIN</option>
                    </SelectInput>
                  </Field>
                  <Button icon={UserPlus} busy={busy === "signup"} className="w-full">
                    Create account
                  </Button>
                </div>
              </form>
            </>
          ) : (
            <>
              <form onSubmit={createCustomer} className="rounded-md bg-white p-5 shadow-soft ring-1 ring-slate-200">
                <h2 className="flex items-center gap-2 text-lg font-bold text-slate-950">
                  <UserPlus className="h-5 w-5 text-emerald-700" />
                  Customer
                </h2>
                <div className="mt-5 space-y-4">
                  <Field label="Name">
                    <TextInput value={customerForm.name} onChange={(e) => setCustomerForm({ ...customerForm, name: e.target.value })} />
                  </Field>
                  <Field label="Email">
                    <TextInput type="text" value={customerForm.email} onChange={(e) => setCustomerForm({ ...customerForm, email: e.target.value })} />
                  </Field>
                  <Field label="Phone">
                    <TextInput value={customerForm.phone} onChange={(e) => setCustomerForm({ ...customerForm, phone: e.target.value })} />
                  </Field>
                  <Button icon={UserPlus} busy={busy === "customer"} className="w-full">
                    Save customer
                  </Button>
                  {customer ? (
                    <div className="rounded-md bg-emerald-50 p-3 text-sm text-emerald-900">
                      #{customer.id} {customer.name}
                    </div>
                  ) : null}
                </div>
              </form>

              <form onSubmit={createBooking} className="rounded-md bg-white p-5 shadow-soft ring-1 ring-slate-200">
                <h2 className="flex items-center gap-2 text-lg font-bold text-slate-950">
                  <CalendarCheck className="h-5 w-5 text-emerald-700" />
                  Booking
                </h2>
                <div className="mt-5 grid gap-4">
                  <Field label="Customer ID">
                    <TextInput value={bookingForm.customerId} onChange={(e) => setBookingForm({ ...bookingForm, customerId: e.target.value })} />
                  </Field>
                  <div className="grid grid-cols-2 gap-3">
                    <Field label="Hotel ID">
                      <TextInput value={bookingForm.hotelId} onChange={(e) => setBookingForm({ ...bookingForm, hotelId: e.target.value })} />
                    </Field>
                    <Field label="Room ID">
                      <TextInput value={bookingForm.roomId} onChange={(e) => setBookingForm({ ...bookingForm, roomId: e.target.value })} />
                    </Field>
                  </div>
                  <div className="grid grid-cols-2 gap-3">
                    <Field label="Check-in">
                      <TextInput type="date" value={bookingForm.checkInDate} onChange={(e) => setBookingForm({ ...bookingForm, checkInDate: e.target.value })} />
                    </Field>
                    <Field label="Check-out">
                      <TextInput type="date" value={bookingForm.checkOutDate} onChange={(e) => setBookingForm({ ...bookingForm, checkOutDate: e.target.value })} />
                    </Field>
                  </div>
                  {selectedRoom ? (
                    <div className="rounded-md bg-sky-50 p-3 text-sm text-sky-900">
                      {selectedRoom.type} at Rs {Number(selectedRoom.price).toLocaleString("en-IN")}
                    </div>
                  ) : null}
                  <Button icon={CalendarCheck} busy={busy === "booking"}>
                    Create booking
                  </Button>
                </div>
              </form>
            </>
          )}
        </aside>

        <section className="space-y-6">
          {error ? (
            <div className="flex items-start gap-3 rounded-md border border-rose-200 bg-rose-50 p-4 text-sm font-medium text-rose-900">
              <X className="mt-0.5 h-4 w-4 shrink-0" />
              <div>
                <div className="flex flex-wrap items-center gap-2">
                  <span>{error.title}</span>
                  {error.status ? (
                    <span className="rounded bg-rose-100 px-2 py-0.5 text-xs font-bold text-rose-700">
                      {error.status}
                    </span>
                  ) : null}
                </div>
                {error.items?.length ? (
                  <ul className="mt-2 list-disc space-y-1 pl-5 text-rose-800">
                    {error.items.map((item) => (
                      <li key={item}>{item}</li>
                    ))}
                  </ul>
                ) : null}
              </div>
            </div>
          ) : null}
          {notice ? (
            <div className="flex items-start gap-3 rounded-md border border-emerald-200 bg-emerald-50 p-4 text-sm font-medium text-emerald-900">
              <Check className="mt-0.5 h-4 w-4 shrink-0" />
              {notice}
            </div>
          ) : null}

          {isAuthed ? (
            <>
              <div className="rounded-md bg-white p-5 shadow-soft ring-1 ring-slate-200">
                <form onSubmit={searchHotels} className="grid gap-4 lg:grid-cols-[1.2fr_1fr_1fr_auto] lg:items-end">
                  <Field label="City" icon={MapPin}>
                    <TextInput placeholder="Delhi" value={hotelFilters.city} onChange={(e) => setHotelFilters({ ...hotelFilters, city: e.target.value })} />
                  </Field>
                  <Field label="Check-in">
                    <TextInput type="date" value={hotelFilters.checkIn} onChange={(e) => setHotelFilters({ ...hotelFilters, checkIn: e.target.value })} />
                  </Field>
                  <Field label="Check-out">
                    <TextInput type="date" value={hotelFilters.checkOut} onChange={(e) => setHotelFilters({ ...hotelFilters, checkOut: e.target.value })} />
                  </Field>
                  <Button icon={Search} busy={busy === "hotels"} disabled={!isAdmin}>
                    Search
                  </Button>
                </form>
                {!isAdmin ? (
                  <p className="mt-3 text-sm font-medium text-amber-700">Hotel and room search needs ADMIN access in the backend security config.</p>
                ) : null}
              </div>

              {isAdmin ? (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                  {hotels.map((hotel) => (
                    <article key={hotel.id} className="rounded-md bg-white p-5 shadow-sm ring-1 ring-slate-200">
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <p className="text-xs font-bold uppercase tracking-normal text-emerald-700">Hotel #{hotel.id}</p>
                          <h3 className="mt-1 text-lg font-bold text-slate-950">{hotel.name}</h3>
                          <p className="mt-2 flex items-center gap-2 text-sm text-slate-500">
                            <MapPin className="h-4 w-4" />
                            {hotel.location}
                          </p>
                        </div>
                        <Building2 className="h-6 w-6 text-slate-300" />
                      </div>
                      <Button icon={DoorOpen} variant="ghost" onClick={() => searchRooms(hotel)} className="mt-4 w-full" busy={busy === "rooms" && activeHotel?.id === hotel.id}>
                        View rooms
                      </Button>
                    </article>
                  ))}
                </div>
              ) : null}

              {isAdmin && hotelsPage && hotels.length === 0 ? <EmptyState title="No hotels found" text="Try another city or date range." /> : null}

              {activeHotel ? (
                <div className="rounded-md bg-white p-5 shadow-soft ring-1 ring-slate-200">
                  <div className="flex flex-wrap items-center justify-between gap-4">
                    <div>
                      <p className="text-sm font-semibold text-emerald-700">Rooms at {activeHotel.name}</p>
                      <h2 className="mt-1 text-2xl font-bold text-slate-950">{totalItems(roomsPage)} matching rooms</h2>
                    </div>
                    <form onSubmit={(e) => { e.preventDefault(); searchRooms(); }} className="grid gap-3 sm:grid-cols-5 sm:items-end">
                      <Field label="Min">
                        <TextInput type="number" value={roomFilters.minPrice} onChange={(e) => setRoomFilters({ ...roomFilters, minPrice: e.target.value })} />
                      </Field>
                      <Field label="Max">
                        <TextInput type="number" value={roomFilters.maxPrice} onChange={(e) => setRoomFilters({ ...roomFilters, maxPrice: e.target.value })} />
                      </Field>
                      <Field label="In">
                        <TextInput type="date" value={roomFilters.checkIn} onChange={(e) => setRoomFilters({ ...roomFilters, checkIn: e.target.value })} />
                      </Field>
                      <Field label="Out">
                        <TextInput type="date" value={roomFilters.checkOut} onChange={(e) => setRoomFilters({ ...roomFilters, checkOut: e.target.value })} />
                      </Field>
                      <Button icon={Search} variant="secondary" busy={busy === "rooms"}>
                        Filter
                      </Button>
                    </form>
                  </div>
                  <div className="mt-5 grid gap-3">
                    {rooms.map((room) => (
                      <div key={room.id} className="grid gap-4 rounded-md border border-slate-200 bg-slate-50 p-4 sm:grid-cols-[1fr_auto_auto] sm:items-center">
                        <div>
                          <p className="text-sm font-bold text-slate-950">{room.type}</p>
                          <p className="mt-1 text-xs font-semibold text-slate-500">Room #{room.id}</p>
                        </div>
                        <p className="flex items-center gap-2 text-base font-bold text-slate-950">
                          <CircleDollarSign className="h-4 w-4 text-emerald-700" />
                          Rs {Number(room.price).toLocaleString("en-IN")}
                        </p>
                        <Button
                          icon={BedDouble}
                          variant="ghost"
                          onClick={() => setBookingForm((prev) => ({ ...prev, hotelId: activeHotel.id, roomId: room.id }))}
                        >
                          Select
                        </Button>
                      </div>
                    ))}
                    {roomsPage && rooms.length === 0 ? <EmptyState title="No rooms found" text="Change the price or stay dates." /> : null}
                  </div>
                </div>
              ) : null}

              <div className="rounded-md bg-white p-5 shadow-soft ring-1 ring-slate-200">
                <div className="flex flex-wrap items-center justify-between gap-4">
                  <h2 className="flex items-center gap-2 text-xl font-bold text-slate-950">
                    <Sparkles className="h-5 w-5 text-amber-600" />
                    Bookings
                  </h2>
                  <form onSubmit={loadBookings} className="grid gap-3 sm:grid-cols-[150px_140px_140px_auto] sm:items-end">
                    <Field label="Status">
                      <SelectInput value={bookingFilters.status} onChange={(e) => setBookingFilters({ ...bookingFilters, status: e.target.value })}>
                        <option value="">All</option>
                        <option value="CONFIRMED">Confirmed</option>
                        <option value="CANCELLED">Cancelled</option>
                      </SelectInput>
                    </Field>
                    <Field label="Customer">
                      <TextInput value={bookingFilters.customerId} onChange={(e) => setBookingFilters({ ...bookingFilters, customerId: e.target.value })} />
                    </Field>
                    <Field label="Hotel">
                      <TextInput value={bookingFilters.hotelId} onChange={(e) => setBookingFilters({ ...bookingFilters, hotelId: e.target.value })} />
                    </Field>
                    <Button icon={Search} variant="secondary" busy={busy === "bookings"}>
                      Load
                    </Button>
                  </form>
                </div>
                <div className="mt-5 overflow-hidden rounded-md border border-slate-200">
                  <div className="grid grid-cols-[90px_1fr_1fr_120px] bg-slate-100 px-4 py-3 text-xs font-bold uppercase tracking-normal text-slate-500">
                    <span>ID</span>
                    <span>Guest</span>
                    <span>Stay</span>
                    <span>Status</span>
                  </div>
                  {bookings.map((booking) => (
                    <div key={booking.bookingId} className="grid gap-3 border-t border-slate-200 bg-white px-4 py-4 md:grid-cols-[90px_1fr_1fr_120px] md:items-center">
                      <span className="font-bold text-slate-950">#{booking.bookingId}</span>
                      <div>
                        <p className="font-semibold text-slate-900">{booking.customerName}</p>
                        <p className="text-sm text-slate-500">{booking.hotelName} · {booking.roomType}</p>
                      </div>
                      <p className="text-sm font-medium text-slate-700">
                        {booking.checkInDate} to {booking.checkOutDate}
                      </p>
                      <div className="flex flex-wrap gap-2">
                        <span className={`rounded-md px-2 py-1 text-xs font-bold ${booking.status === "CANCELLED" ? "bg-rose-50 text-rose-700" : "bg-emerald-50 text-emerald-700"}`}>
                          {booking.status}
                        </span>
                        {booking.status !== "CONFIRMED" ? (
                          <Button variant="ghost" icon={Check} onClick={() => updateBooking(booking.bookingId, "confirm")} busy={busy === `confirm-${booking.bookingId}`}>
                            Confirm
                          </Button>
                        ) : null}
                        {booking.status !== "CANCELLED" ? (
                          <Button variant="danger" icon={X} onClick={() => updateBooking(booking.bookingId, "cancel")} busy={busy === `cancel-${booking.bookingId}`}>
                            Cancel
                          </Button>
                        ) : null}
                      </div>
                    </div>
                  ))}
                  {bookingsPage && bookings.length === 0 ? (
                    <div className="border-t border-slate-200 p-4">
                      <EmptyState title="No bookings loaded" text="Create a booking or adjust filters." />
                    </div>
                  ) : null}
                </div>
              </div>
            </>
          ) : (
            <EmptyState title="Sign in to open the dashboard" text="Use an ADMIN account for hotel and room management, or STAFF for customers and bookings." />
          )}
        </section>
      </main>
    </div>
  );
}
